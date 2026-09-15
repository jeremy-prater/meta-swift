"""
Fold SwiftPM's SBOM into a recipe's OpenEmbedded SPDX 3.0 documents.

SwiftPM (Swift 6.4+, SE-0509) can describe the package graph it resolved for a
build. Those dependencies are fetched by SwiftPM during do_swift_package_resolve
and do_compile, not through SRC_URI, so OE's own SPDX has no record of them.
This adds each SwiftPM package and product to the recipe's build document,
keeps SwiftPM's dependency edges, records the SwiftPM packages as inputs of the
build (as OE does for SRC_URI downloads), and records the runtime packages in
SWIFT_SPDX_STATIC_LINK_PACKAGES as statically linking the products the root
package uses. Image and SDK SBOMs then pick them up through the existing
package -> build links.

The input is SwiftPM's CycloneDX output rather than its SPDX output: SwiftPM
checks the CycloneDX against its schema and records the commit and repository
of every dependency, whereas its SPDX uses properties that are not part of
SPDX 3.0.1 (externalUrl, software_internalVersion).

Known limitations:
- With the native build system SwiftPM cannot apply build-time conditionals,
  so the graph can include dependencies that are never built for the target,
  and macro-only dependencies (e.g. swift-syntax) are reported as required.
- The CycloneDX is written by do_compile. If do_create_spdx runs while
  do_compile was restored from sstate there is nothing to fold in, and a
  warning is emitted.
"""

import json
import re
import urllib.parse
from pathlib import Path

# Prefix for the SPDX IDs of SwiftPM elements, keeping them apart from the
# IDs OE itself allocates in the build document (source, build, license...).
SPDX_ID_PREFIX = "swiftpm"


def _swift_entity(component):
    for prop in component.get("properties") or []:
        if prop.get("name") == "swift-entity":
            return prop.get("value")
    return None


def find_cyclonedx(sbom_dir):
    """Return the newest SwiftPM CycloneDX SBOM in sbom_dir, or None."""
    candidates = sorted(
        Path(sbom_dir).glob("cyclonedx*.json"), key=lambda p: p.stat().st_mtime
    )
    return candidates[-1] if candidates else None


def repo_uri(url):
    """
    Return a SwiftPM dependency's repository location as an absolute URI with
    no user information, or None if it is not a remote repository.

    SwiftPM records locations as written in the manifest, so SSH dependencies
    are often scp-style (git@github.com:org/repo), which is not a URI. User
    names and credentials are dropped, as OE does for SRC_URI download
    locations.
    """
    if not url:
        return None

    if "://" not in url:
        m = re.match(r"^(?:[^@/]+@)?([^:/]+):(.+)$", url)
        if not m:
            # A local path
            return None
        return f"ssh://{m.group(1)}/{m.group(2).lstrip('/')}"

    parts = urllib.parse.urlsplit(url)
    if parts.scheme == "file" or not parts.hostname:
        return None
    host = f"[{parts.hostname}]" if ":" in parts.hostname else parts.hostname
    try:
        port = parts.port
    except ValueError:
        port = None
    netloc = host if port is None else f"{host}:{port}"
    return urllib.parse.urlunsplit(
        (parts.scheme, netloc, parts.path, parts.query, parts.fragment)
    )


def add_cyclonedx_to_build(objset, build, bom):
    """
    Add the components of a SwiftPM CycloneDX SBOM to objset, a recipe's build
    document, as inputs of build.

    The CycloneDX root component is the recipe's own package, and it and its
    products are skipped: OE already describes those. Returns the link IDs of
    the SwiftPM products the root package depends on directly.
    """
    import bb
    import oe.sbom30
    import oe.spdx30

    root = bom["metadata"]["component"]["bom-ref"]

    def is_root(ref):
        return ref == root or ref.startswith(root + ":")

    purposes = {
        "application": oe.spdx30.software_SoftwarePurpose.application,
        "library": oe.spdx30.software_SoftwarePurpose.library,
    }

    elements = {}
    entities = {}
    for component in bom.get("components") or []:
        ref = component["bom-ref"]
        entities[ref] = _swift_entity(component)
        if is_root(ref):
            continue

        spdxid = objset.new_spdxid(SPDX_ID_PREFIX, ref)
        if objset.find_by_id(spdxid) is not None:
            bb.warn(f"SwiftPM SBOM: {ref} maps to duplicate SPDX ID {spdxid}, skipping")
            continue

        pkg = objset.add(
            oe.spdx30.software_Package(
                _id=spdxid,
                creationInfo=objset.doc.creationInfo,
                name=component["name"],
                software_primaryPurpose=purposes.get(
                    component.get("type"), oe.spdx30.software_SoftwarePurpose.library
                ),
            )
        )
        if entities[ref]:
            pkg.summary = entities[ref]
        if component.get("version"):
            pkg.software_packageVersion = component["version"]

        purl = component.get("purl")
        if purl:
            pkg.software_packageUrl = purl
            pkg.externalIdentifier.append(
                oe.spdx30.ExternalIdentifier(
                    externalIdentifierType=oe.spdx30.ExternalIdentifierType.packageUrl,
                    identifier=purl,
                )
            )

        # Same download location form OE uses for git SRC_URI entries.
        commits = (component.get("pedigree") or {}).get("commits") or []
        url = repo_uri(commits[0].get("url")) if commits else None
        if url:
            uid = commits[0].get("uid")
            pkg.software_downloadLocation = f"git+{url}@{uid}" if uid else f"git+{url}"
            pkg.externalRef.append(
                oe.spdx30.ExternalRef(
                    externalRefType=oe.spdx30.ExternalRefType.vcs,
                    locator=[url],
                )
            )

        objset.set_element_alias(pkg)
        elements[ref] = pkg

    def link_ids(refs):
        return sorted(
            oe.sbom30.get_element_link_id(elements[r]) for r in set(refs) if r in elements
        )

    for dep in bom.get("dependencies") or []:
        ref = dep["ref"]
        if ref not in elements:
            continue
        depends_on = dep.get("dependsOn") or []
        # SwiftPM lists a package's own products as its dependencies.
        products = link_ids(r for r in depends_on if r.startswith(ref + ":"))
        others = link_ids(r for r in depends_on if not r.startswith(ref + ":"))
        if products:
            objset.new_relationship(
                [elements[ref]], oe.spdx30.RelationshipType.contains, products
            )
        if others:
            objset.new_relationship(
                [elements[ref]], oe.spdx30.RelationshipType.dependsOn, others
            )

    packages = link_ids(r for r in elements if entities.get(r) == "swift-package")
    if packages:
        objset.new_scoped_relationship(
            [build],
            oe.spdx30.RelationshipType.hasInput,
            oe.spdx30.LifecycleScopeType.build,
            packages,
        )

    root_deps = set()
    for dep in bom.get("dependencies") or []:
        if is_root(dep["ref"]):
            root_deps.update(dep.get("dependsOn") or [])
    return link_ids(r for r in root_deps if entities.get(r) == "swift-product")


def merge_cyclonedx(d, bom, deploydir, static_link_pkg_names):
    """
    Merge bom into the build and staging package documents that create_spdx
    wrote under deploydir.
    """
    import bb
    import oe.sbom30
    import oe.spdx30

    pkg_arch = d.getVar("SSTATE_PKGARCH")
    pn = d.getVar("PN")

    build_path = oe.sbom30.jsonld_arch_path(
        d, pkg_arch, "builds", "build-" + pn, deploydir=deploydir
    )
    build_objset = oe.sbom30.load_jsonld(d, build_path, required=True)
    build = build_objset.find_root(oe.spdx30.build_Build)
    if build is None:
        bb.fatal(f"No build found in {build_path}")

    static_links = add_cyclonedx_to_build(build_objset, build, bom)

    # create_spdx has already made this document's by-spdxid-hash link, which
    # write_recipe_jsonld_doc would try to create again, so rewrite in place.
    build_objset.add_aliases()
    oe.sbom30.write_jsonld_doc(d, build_objset, build_path)

    if not static_links:
        return

    for pkg_name in static_link_pkg_names:
        pkg_path = oe.sbom30.jsonld_arch_path(
            d, pkg_arch, "packages-staging", "package-" + pkg_name, deploydir=deploydir
        )
        pkg_objset = oe.sbom30.load_jsonld(d, pkg_path, required=True)
        spdx_package = pkg_objset.find_root(oe.spdx30.software_Package)
        if spdx_package is None:
            bb.fatal(f"No package found in {pkg_path}")

        pkg_objset.new_relationship(
            [spdx_package], oe.spdx30.RelationshipType.hasStaticLink, static_links
        )
        oe.sbom30.write_recipe_jsonld_doc(
            d, pkg_objset, "packages-staging", deploydir, create_spdx_id_links=False
        )


def add_swiftpm_sbom(d):
    """do_create_spdx postfunc; see the module docstring."""
    if d.getVar("SWIFT_SPDX") != "1":
        return

    import bb
    import oe.packagedata
    import oe.spdx30_tasks

    if oe.spdx30_tasks.get_is_native(d):
        return

    sbom_dir = d.getVar("SWIFT_SPDX_SBOM_DIR")
    cyclonedx = find_cyclonedx(sbom_dir)
    if cyclonedx is None:
        bb.warn(
            f"No SwiftPM CycloneDX SBOM in {sbom_dir}, so the SPDX for "
            f"{d.getVar('PN')} does not include its SwiftPM dependencies. Either "
            "SwiftPM could not write it (see the do_compile log) or do_compile "
            "was restored from sstate while do_create_spdx was not."
        )
        return

    with cyclonedx.open() as f:
        bom = json.load(f)

    bb.build.exec_func("read_subpackage_metadata", d)
    pkg_names = [
        d.getVar("PKG:%s" % package) or package
        for package in (d.getVar("SWIFT_SPDX_STATIC_LINK_PACKAGES") or "").split()
        if oe.packagedata.packaged(package, d)
    ]

    merge_cyclonedx(d, bom, Path(d.getVar("SPDXDEPLOY")), pkg_names)

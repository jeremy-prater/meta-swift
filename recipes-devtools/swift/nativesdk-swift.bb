SUMMARY = "Swift toolchain bundle for the populate_sdk host (swift.org prebuilt)"
HOMEPAGE = "https://swift.org/install/"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/usr/share/swift/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

# Pin the bundle version to whatever swift-native (meta-swift) is using so
# the SDK's host swiftc/clang match the target stdlib's build provenance.
require swift-version.inc
PV = "${SWIFT_VERSION}"

# swift.org publishes per-host-arch tarballs (x86_64 unsuffixed, aarch64
# "-aarch64"). Use SDK_ARCH so we pick the binary for the SDK host machine.
def swift_sdk_arch_suffix(d):
    sdk_arch = d.getVar('SDK_ARCH')
    return '' if sdk_arch == 'x86_64' else '-{}'.format(sdk_arch)

def swift_sdk_arch_checksum(d):
    sha256 = {
        "x86_64":  "648daccc9062045cb42431f6c7d620858b729abccb6bb9075a6b990e6259e897",
        "aarch64": "8b85ce9e2a13802654e2a097d8720f0726d0900adc3579af1649190cc6cd49be",
    }
    sdk_arch = d.getVar('SDK_ARCH')
    if sdk_arch not in sha256:
        bb.fatal('Unsupported SDK_ARCH for nativesdk-swift: {}'.format(sdk_arch))
    return sha256[sdk_arch]

# The bundle's ELF interpreter is the host loader (arch-specific); pick it by
# SDK_ARCH so the .interp padding below targets the right one.
def swift_sdk_arch_interpreter(d):
    sdk_arch = d.getVar('SDK_ARCH')
    if sdk_arch == 'x86_64':
        return 'ld-linux-x86-64.so.2'
    if sdk_arch == 'aarch64':
        return 'ld-linux-aarch64.so.1'
    bb.fatal('Unsupported SDK_ARCH for nativesdk-swift: {}'.format(sdk_arch))

SWIFT_ARCH_SUFFIX = "${@swift_sdk_arch_suffix(d)}"
SWIFT_SDK_INTERPRETER = "${@swift_sdk_arch_interpreter(d)}"
SWIFT_LINUX_DISTRO = "amazonlinux2"

SRC_DIR = "${SWIFT_TAG}-${SWIFT_LINUX_DISTRO}${SWIFT_ARCH_SUFFIX}"
SRC_URI = "https://download.swift.org/swift-${SWIFT_VERSION}-release/${SWIFT_LINUX_DISTRO}${SWIFT_ARCH_SUFFIX}/${SWIFT_TAG}/${SWIFT_TAG}-${SWIFT_LINUX_DISTRO}${SWIFT_ARCH_SUFFIX}.tar.gz"
SRC_URI[sha256sum] = "${@swift_sdk_arch_checksum(d)}"

S = "${UNPACKDIR}/${SRC_DIR}"

DEPENDS = "patchelf-native"

# swift.org only publishes x86_64 and aarch64 host bundles; the checksum/
# interpreter maps above only know those two. Restrict the recipe accordingly.
COMPATIBLE_HOST = "(x86_64|aarch64).*-linux*"

inherit nativesdk

do_install() {
    install -d ${D}${bindir} ${D}${libdir} ${D}${includedir} ${D}${datadir}
    cp -rd ${S}/usr/bin/.     ${D}${bindir}/
    cp -rd ${S}/usr/lib/.     ${D}${libdir}/
    cp -rd ${S}/usr/include/. ${D}${includedir}/
    cp -rd ${S}/usr/share/.   ${D}${datadir}/
    if [ -d ${S}/usr/libexec ]; then
        install -d ${D}${libexecdir}
        cp -rd ${S}/usr/libexec/. ${D}${libexecdir}/
    fi

    # Drop the bundle's host-side libdispatch/Foundation/Block/os headers and
    # modulemaps: meta-swift already provides target-arch versions in the sysroot,
    # so keeping both makes swiftc see duplicate module definitions. Note that
    # dispatch/Block sit at ${libdir}/{dispatch,Block} in this bundle, while the
    # rest live under ${libdir}/swift/.
    rm -rf ${D}${libdir}/dispatch ${D}${libdir}/Block
    for d in os CoreFoundation _FoundationCShims _foundation_unicode shims; do
        rm -rf ${D}${libdir}/swift/${d}
    done

    # swift.org binaries have a short .interp; Yocto's relocate_sdk.py aborts when
    # the SDK install path is >= p_filesz, so grow the slot with patchelf. Anchor
    # the placeholder with ${SDKPATH}/ (not /lib*, which relocate_sdk.py skips as
    # an external marker) so the relocator still processes it, then pad.
    pad=$(printf '%440s' '' | tr ' ' 'x')
    long_interp="${SDKPATH}/${pad}/lib/${SWIFT_SDK_INTERPRETER}"
    for f in ${D}${bindir}/* ${D}${libexecdir}/swift/*; do
        [ -f "$f" ] || continue
        # Only ELF objects that already carry a PT_INTERP need (and accept) an
        # interpreter rewrite; --print-interpreter cleanly skips scripts, static
        # binaries and plain shared libs. A failure here is fatal so a broken
        # patch surfaces now rather than as a cryptic relocate_sdk.py abort.
        patchelf --print-interpreter "$f" >/dev/null 2>&1 || continue
        patchelf --set-interpreter "$long_interp" "$f" || \
            bbfatal "patchelf --set-interpreter failed on $f"
    done
}

# The swift.org bundle is self-contained and pre-stripped. Don't let
# Yocto's QA / packaging machinery rewrite it beyond .interp padding.
INSANE_SKIP:${PN} = "already-stripped libdir staticdev arch dev-so dev-elf textrel file-rdeps"
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT = "1"
INHIBIT_SYSROOT_STRIP = "1"

# The bundle is self-contained (its libs are found via $ORIGIN RPATH), but RPM's
# auto-provides scanner misses the bundled SONAMEs and emits bad Requires:; skip
# file-based dep generation entirely.
SKIP_FILEDEPS:${PN} = "1"

# Single package; everything goes to the SDK host sysroot.
PACKAGES = "${PN}"
FILES:${PN} = " \
    ${bindir} \
    ${libdir} \
    ${includedir} \
    ${datadir} \
    ${libexecdir} \
"

SUMMARY = "Swift toolchain bundle for the populate_sdk host (swift.org prebuilt)"
HOMEPAGE = "https://swift.org/install/"

# The bundle must run on the SDK host machine, not the build host: pick the
# per-arch tarball by SDK_ARCH. Everything else about the bundle (URL scheme,
# suffix rules, version pin, checksums, S, licence) comes from the shared
# tarball .inc so the SDK's host swiftc/clang always match the exact bundle
# the native toolchain recipes use.
SWIFT_BUNDLE_ARCH = "${SDK_ARCH}"
require swift-native-tarball.inc

# The bundle's ELF interpreter is the host loader (arch-specific); pick it by
# SDK_ARCH so the .interp padding below targets the right one.
def swift_sdk_arch_interpreter(d):
    sdk_arch = d.getVar('SDK_ARCH')
    if sdk_arch == 'x86_64':
        return 'ld-linux-x86-64.so.2'
    if sdk_arch == 'aarch64':
        return 'ld-linux-aarch64.so.1'
    bb.fatal('Unsupported SDK_ARCH for nativesdk-swift: {}'.format(sdk_arch))

SWIFT_SDK_INTERPRETER = "${@swift_sdk_arch_interpreter(d)}"

DEPENDS = "patchelf-native"

# swift.org only publishes x86_64 and aarch64 host bundles; the shared checksum
# map and the interpreter map above only know those two. Restrict accordingly.
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

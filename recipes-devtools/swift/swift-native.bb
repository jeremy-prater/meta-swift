SUMMARY = "Swift native toolchain, built from source"
DESCRIPTION = "Host Swift toolchain (compiler, SwiftPM, host stdlib) built from \
upstream swiftlang sources. Bootstrapped via virtual/swift-bootstrap-native \
(the swift.org tarball by default, or swift-stage0-native for a build with no \
prebuilt Swift anywhere in it). \
Alternative provider for virtual/swift-native. The layer default is \
swift-binary-native (the prebuilt swift.org tarball); set \
PREFERRED_PROVIDER_virtual/swift-native = \"swift-native\" to select \
this recipe."
HOMEPAGE = "https://swift.org/install/"

LICENSE = "Apache-2.0"

require swift-version.inc
require swift-source.inc
require swift-bootstrap-paths.inc
PV = "${SWIFT_VERSION}+git"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI = "\
    file://yocto-native.preset.ini \
${SWIFT_SRC_URI}"


SWIFT_PRESET_FILE = "${UNPACKDIR}/yocto-native.preset.ini"

LIC_FILES_CHKSUM = "file://${UNPACKDIR}/swift/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

S = "${UNPACKDIR}/swift"

PROVIDES += "virtual/swift-native"

DEPENDS = "virtual/swift-bootstrap-native python3-native cmake-native ninja-native pkgconfig-native libxml2-native sqlite3-native curl-native ncurses-native rsync-native"

inherit native

do_configure[network] = "0"
do_compile[network] = "0"

do_configure() {
    export PATH="${SWIFT_BOOTSTRAP_PATH}:${PATH}"

    # swift's cmake wraps its custom swiftc invocations in `cmake -E env
    # LD_LIBRARY_PATH=<bootstrap>/lib/swift/linux`, which clobbers any
    # outer LD_LIBRARY_PATH we set. The freshly-built swiftc (and its
    # earlyswiftdriver deps libSwiftDriverExecution.so, libllbuildSwift.so)
    # gain DT_NEEDED: libncurses.so.5 + libtinfo.so.5 via TSC's
    # TerminalController. Mirror every versioned shared-object from
    # ${STAGING_DIR_NATIVE}/usr/lib into the cmake-env path so the in-build
    # swiftc invocations can resolve whatever version-suffixed libs get
    # linked in.
    #
    # ABI caveat: this works only while OE-core's ncurses soname matches
    # what the Amazon Linux 2 bootstrap toolchain was built against. If
    # those diverge (e.g. OE bumps past soname 5 before the bootstrap
    # does), the symlinks still get created but dlopen will fail -- at
    # which point we'd need to vendor a compatible libncurses into
    # swift-bootstrap-native itself rather than rely on the host sysroot.
    for lib in ${STAGING_DIR_NATIVE}/usr/lib/lib*.so.*; do
        [ -e "$lib" ] || continue
        ln -sfn "$lib" ${SWIFT_BOOTSTRAP_PREFIX}/lib/swift/linux/$(basename "$lib")
    done
}

# build-script drives configure + build + install in one shot; do_install
# is responsible only for relocating the output into the staging layout.
do_compile() {
    export PATH="${SWIFT_BOOTSTRAP_PATH}:${PATH}"

    # swift-tools-support-core's TerminalController imports ncurses, so the
    # earlyswiftdriver-stage libraries (libSwiftDriverExecution.so,
    # libllbuildSwift.so) end up with DT_NEEDED: libncurses.so.5. ncurses-
    # native provides it at ${STAGING_DIR_NATIVE}/usr/lib, but that path
    # isn't on the default ld.so search path -- export it so the freshly
    # built swiftc can run during the subsequent stdlib build.
    export LD_LIBRARY_PATH="${STAGING_DIR_NATIVE}/usr/lib${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}"

    # OE's native LDFLAGS (-Wl,--enable-new-dtags etc.) land in cmake's
    # env and get passed verbatim to swiftc when libdispatch links
    # libswiftDispatch.so via Swift -- swiftc rejects bare -Wl, flags.
    # Swift's build-script manages its own linker options, so discard.
    unset LDFLAGS
    unset BUILD_LDFLAGS

    # swiftpm/Utilities/bootstrap's build_with_cmake passes CMAKE_C_COMPILER
    # but not CMAKE_CXX_COMPILER, so per-dep CMake falls through to PATH and
    # picks OE's hosttools/g++ for swift-build's SWBCSupport/CLibclang.cpp
    # (which uses -fblocks, clang-only). Pair CXX with the just-built
    # clang++ that --clang-path already points at for CC.
    export CXX="${SWIFT_BOOTSTRAP_PATH}/clang++"

    # The incremental build tree is only valid for the toolchain that built
    # it: the bootstrap provider can change (tarball <-> stage0) without
    # touching do_unpack's inputs, and ninja's timestamp checks would then do
    # a tiny incremental rebuild mixing objects from different compilers --
    # observed as bogus module-package errors in one direction and raw ELF
    # garbage in the build log in the other. Key the tree on the bootstrap's
    # actual identity (and the install destdir, which build-script bakes into
    # its cmake caches) and wipe only on mismatch, so a retry after a
    # transient failure keeps its hours of completed objects.
    # NB the tree is NOT ${S}/build: build-script's workspace is the PARENT
    # of swift/ (each repo a sibling), so it builds under ${UNPACKDIR}/build.
    bootstrap_id="$(${SWIFT_BOOTSTRAP_PATH}/swiftc --version 2>&1; ${SWIFT_BOOTSTRAP_PATH}/clang --version 2>&1 | head -1; echo destdir=${WORKDIR}/dest)"
    stamp=${UNPACKDIR}/build/.oe-bootstrap-id
    if [ ! -f "$stamp" ] || [ "$(cat "$stamp")" != "$bootstrap_id" ]; then
        rm -rf ${UNPACKDIR}/build
        mkdir -p ${UNPACKDIR}/build
        printf '%s' "$bootstrap_id" > "$stamp"
    fi
    # build-script's install step fully regenerates the dest tree on every
    # run; wiping it here prevents files removed from the install set ever
    # lingering from a previous configuration.
    rm -rf ${WORKDIR}/dest

    cd ${S}
    ./utils/build-script \
        --preset-file=${SWIFT_PRESET_FILE} \
        --preset=yocto_native \
        install_destdir=${WORKDIR}/dest \
        -j ${@oe.utils.parallel_make(d) or 1}
}

do_install() {
    # build-script installs into ${WORKDIR}/dest during do_compile -- NOT
    # into ${D}, which would need do_install[cleandirs] disabled to survive
    # between tasks; a persistent ${D} is exactly what previously let a stale
    # tree be silently re-staged (mv into an existing directory NESTS rather
    # than replaces). With the default cleandirs back, ${D} is freshly wiped
    # every run; hardlink-copy the staged tree in (same filesystem, no data
    # copied) so ${WORKDIR}/dest survives for do_install-only re-runs.
    # native.bbclass's populate_sysroot only captures ${D}${STAGING_DIR_NATIVE}/.
    [ -d ${WORKDIR}/dest/usr ] || bbfatal "no build-script install tree at ${WORKDIR}/dest/usr"
    install -d ${D}${STAGING_DIR_NATIVE}
    cp -al ${WORKDIR}/dest/usr ${D}${STAGING_DIR_NATIVE}/usr
}

PACKAGES = "${PN}"
FILES:${PN} = "${bindir} ${libdir} ${includedir} ${datadir}"

INSANE_SKIP:${PN} = "already-stripped arch buildpaths dev-so file-rdeps libdir staticdev"

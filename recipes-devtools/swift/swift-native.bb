SUMMARY = "Swift native toolchain, built from source"
DESCRIPTION = "Host Swift toolchain (compiler, SwiftPM, host stdlib) built from \
upstream swiftlang sources. Bootstrapped using swift-bootstrap-native. \
Alternative provider for virtual/swift-native. The layer default is \
swift-binary-native (the prebuilt swift.org tarball); set \
PREFERRED_PROVIDER_virtual/swift-native = \"swift-native\" to select \
this recipe."
HOMEPAGE = "https://swift.org/install/"

LICENSE = "Apache-2.0"

require swift-version.inc
PV = "${SWIFT_VERSION}+git${SRCPV}"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRCREV_FORMAT = "llvm_swift_cmark_libdispatch_corelibsxctest_swifttesting_foundation_corelibsfoundation_foundationicu_stringproc_syntax_driver_tsc_swiftpm_llbuild_argumentparser_collections_system_toolchainsqlite_toolsprotocols_docc_doccsymbolkit_doccrenderartifact_markdown_format_asn1_certificates_crypto_atomics_log_numerics_subprocess_build"

# destsuffix values match update-checkout-config.json's on-disk names so
# utils/build-script finds each repo as a sibling of swift/.
SRC_URI = "\
    file://yocto-native.preset.ini \
    git://github.com/swiftlang/llvm-project.git;protocol=https;name=llvm;tag=${SWIFT_TAG};nobranch=1;destsuffix=llvm-project; \
    git://github.com/swiftlang/swift.git;protocol=https;name=swift;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift; \
    git://github.com/swiftlang/swift-cmark.git;protocol=https;name=cmark;tag=${SWIFT_TAG};nobranch=1;destsuffix=cmark; \
    git://github.com/swiftlang/swift-corelibs-libdispatch.git;protocol=https;name=libdispatch;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-corelibs-libdispatch; \
    git://github.com/swiftlang/swift-corelibs-xctest.git;protocol=https;name=corelibsxctest;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-corelibs-xctest; \
    git://github.com/swiftlang/swift-testing.git;protocol=https;name=swifttesting;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-testing; \
    git://github.com/swiftlang/swift-foundation.git;protocol=https;name=foundation;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-foundation; \
    git://github.com/swiftlang/swift-corelibs-foundation.git;protocol=https;name=corelibsfoundation;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-corelibs-foundation; \
    git://github.com/swiftlang/swift-foundation-icu.git;protocol=https;name=foundationicu;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-foundation-icu; \
    git://github.com/swiftlang/swift-experimental-string-processing.git;protocol=https;name=stringproc;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-experimental-string-processing; \
    git://github.com/swiftlang/swift-syntax.git;protocol=https;name=syntax;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-syntax; \
    git://github.com/swiftlang/swift-driver.git;protocol=https;name=driver;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-driver; \
    git://github.com/swiftlang/swift-tools-support-core.git;protocol=https;name=tsc;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-tools-support-core; \
    git://github.com/swiftlang/swift-package-manager.git;protocol=https;name=swiftpm;tag=${SWIFT_TAG};nobranch=1;destsuffix=swiftpm; \
    git://github.com/swiftlang/swift-llbuild.git;protocol=https;name=llbuild;tag=${SWIFT_TAG};nobranch=1;destsuffix=llbuild; \
    git://github.com/apple/swift-argument-parser.git;protocol=https;name=argumentparser;tag=${SWIFT_ARGUMENT_PARSER_VERSION};nobranch=1;destsuffix=swift-argument-parser; \
    git://github.com/apple/swift-collections.git;protocol=https;name=collections;tag=${SWIFT_COLLECTIONS_VERSION};nobranch=1;destsuffix=swift-collections; \
    git://github.com/apple/swift-system.git;protocol=https;name=system;tag=${SWIFT_SYSTEM_VERSION};nobranch=1;destsuffix=swift-system; \
    git://github.com/swiftlang/swift-toolchain-sqlite.git;protocol=https;name=toolchainsqlite;tag=${SWIFT_TOOLCHAIN_SQLITE_VERSION};nobranch=1;destsuffix=swift-toolchain-sqlite; \
    git://github.com/swiftlang/swift-tools-protocols.git;protocol=https;name=toolsprotocols;tag=${SWIFT_TOOLS_PROTOCOLS_VERSION};nobranch=1;destsuffix=swift-tools-protocols; \
    git://github.com/swiftlang/swift-docc.git;protocol=https;name=docc;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-docc; \
    git://github.com/swiftlang/swift-docc-symbolkit.git;protocol=https;name=doccsymbolkit;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-docc-symbolkit; \
    git://github.com/swiftlang/swift-docc-render-artifact.git;protocol=https;name=doccrenderartifact;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-docc-render-artifact; \
    git://github.com/swiftlang/swift-markdown.git;protocol=https;name=markdown;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-markdown; \
    git://github.com/swiftlang/swift-format.git;protocol=https;name=format;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-format; \
    git://github.com/apple/swift-asn1.git;protocol=https;name=asn1;tag=${SWIFT_ASN1_VERSION};nobranch=1;destsuffix=swift-asn1; \
    git://github.com/apple/swift-certificates.git;protocol=https;name=certificates;tag=${SWIFT_CERTIFICATES_VERSION};nobranch=1;destsuffix=swift-certificates; \
    git://github.com/apple/swift-crypto.git;protocol=https;name=crypto;tag=${SWIFT_CRYPTO_VERSION};nobranch=1;destsuffix=swift-crypto; \
    git://github.com/apple/swift-atomics.git;protocol=https;name=atomics;tag=${SWIFT_ATOMICS_VERSION};nobranch=1;destsuffix=swift-atomics; \
    git://github.com/apple/swift-log.git;protocol=https;name=log;tag=${SWIFT_LOG_VERSION};nobranch=1;destsuffix=swift-log; \
    git://github.com/apple/swift-numerics.git;protocol=https;name=numerics;tag=${SWIFT_NUMERICS_VERSION};nobranch=1;destsuffix=swift-numerics; \
    git://github.com/swiftlang/swift-subprocess.git;protocol=https;name=subprocess;tag=${SWIFT_SUBPROCESS_VERSION};nobranch=1;destsuffix=swift-subprocess; \
    git://github.com/swiftlang/swift-build.git;protocol=https;name=build;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-build; \
"

SWIFT_PRESET_FILE = "${UNPACKDIR}/yocto-native.preset.ini"

LIC_FILES_CHKSUM = "file://${UNPACKDIR}/swift/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

S = "${UNPACKDIR}/swift"

SWIFT_BUILD_ID = "yocto-${BUILD_ARCH}"

PROVIDES += "virtual/swift-native"

DEPENDS = "swift-bootstrap-native python3-native cmake-native ninja-native pkgconfig-native libxml2-native sqlite3-native curl-native ncurses-native rsync-native"

inherit native

SWIFT_BOOTSTRAP_PATH = "${STAGING_DIR_NATIVE}/opt/swift-bootstrap/usr/bin"

do_configure[network] = "0"
do_compile[network] = "0"

do_configure() {
    export PATH="${SWIFT_BOOTSTRAP_PATH}:${PATH}"

    echo "==== UNPACKDIR sibling layout ===="
    ls -la ${UNPACKDIR}
    echo "==== bootstrap swiftc ===="
    which swiftc
    swiftc --version

    for d in llvm-project swift cmark swift-corelibs-libdispatch \
             swift-corelibs-xctest swift-testing \
             swift-foundation swift-corelibs-foundation swift-foundation-icu \
             swift-experimental-string-processing swift-syntax \
             swift-driver swift-tools-support-core swiftpm llbuild \
             swift-argument-parser swift-collections swift-system \
             swift-toolchain-sqlite swift-tools-protocols \
             swift-docc swift-docc-symbolkit swift-docc-render-artifact \
             swift-markdown swift-format \
             swift-asn1 swift-certificates swift-crypto \
             swift-atomics swift-log swift-numerics swift-subprocess \
             swift-build; do
        if [ ! -d "${UNPACKDIR}/$d" ]; then
            bbfatal "missing repo $d under ${UNPACKDIR}"
        fi
    done

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
        ln -sfn "$lib" ${STAGING_DIR_NATIVE}/opt/swift-bootstrap/usr/lib/swift/linux/$(basename "$lib")
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

    cd ${S}
    ./utils/build-script \
        --preset-file=${SWIFT_PRESET_FILE} \
        --preset=yocto_native \
        install_destdir=${D} \
        -j ${BB_NUMBER_THREADS}
}

# do_compile wrote into ${D}/usr via build-script; base.bbclass's default
# do_install[cleandirs] = "${D}" would wipe it. Keep those files.
do_install[cleandirs] = ""

do_install() {
    # native.bbclass's populate_sysroot only captures ${D}${STAGING_DIR_NATIVE}/.
    # build-script installed to ${D}/usr; relocate via a single rename (same
    # filesystem, no bytes copied).
    install -d ${D}${STAGING_DIR_NATIVE}
    mv ${D}/usr ${D}${STAGING_DIR_NATIVE}/usr
}

PACKAGES = "${PN}"
FILES:${PN} = "${bindir} ${libdir} ${includedir} ${datadir}"

INSANE_SKIP:${PN} = "already-stripped arch buildpaths dev-so file-rdeps libdir staticdev"

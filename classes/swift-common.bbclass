# avoid conflicts with meta-clang
TOOLCHAIN = "gcc"

DEPENDS:append = " swift-native glibc gcc libgcc"
EXTRANATIVEPATH:append = " swift-tools"

python() {
    # Set UNPACKDIR to WORKDIR for Yocto versions older than Styhead
    if d.getVar('UNPACKDIR') is None:
        d.setVar('UNPACKDIR', d.getVar('WORKDIR'))
}

python () {
    # Determine SWIFT_GCC_VERSION by examining bitbake's context dictionary key
    # RECIPE_MAINTAINER:pn-gcc-source-<version>
    gcc_src_maint_pkg = [x for x in d if x.startswith("RECIPE_MAINTAINER:pn-gcc-source-")][0]
    gcc_ver = gcc_src_maint_pkg.rpartition("-")[2]

    d.setVar("SWIFT_GCC_VERSION", gcc_ver)
}

SWIFT_CLANG_VERSION = "21"

# Swift's LLVM triple parser silently normalises any Yocto triple (e.g.
# aarch64-poky-linux, armv7-poky-linux-gnueabihf) down to the LLVM-canonical
# <arch>-unknown-linux-<env> when it writes the per-target .swiftmodule /
# .swiftinterface files inside a merged .swiftmodule directory. If we told
# downstream swiftc invocations to look under the Yocto triple, the stdlib's
# files would only be reachable under the LLVM-canonical name and swiftc
# rejects the "close match" with e.g. "could not find module 'Swift' for
# target 'aarch64-poky-linux'; found: aarch64-unknown-linux-gnu". Force the
# LLVM-canonical triple here so the -target we pass and the filename the
# stdlib actually emitted agree.
#
# On 32-bit arm, TARGET_ARCH is the bare "arm" token, which swiftc doesn't
# recognise as a platform (it warns "unknown platform, assuming
# -mfloat-abi=soft" and looks for the per-arch SwiftGlibc modulemap under an
# "arm" dir while the stdlib stages it under "armv7" - SWIFT_TARGET_ARCH).
# Yocto keeps the float ABI out of TARGET_SYS itself and instead conveys it
# via -mfloat-abi=hard in TARGET_CC_ARCH. That -Xcc flag only reaches the
# Clang importer, but swiftc's own codegen takes the float ABI from the
# triple, so a bare gnueabi triple would be soft-float against a hard-float
# (e.g. cortexa15t2hf) sysroot. Promote the arch token to armv7 and pick the
# eabi environment from ARMPKGSFX_EABI, which Yocto's feature-arm-vfp.inc
# already sets to "hf" when callconvention-hard is in TUNE_FEATURES and to
# "" otherwise, so hardfloat and softfloat ARM tunes each get the triple
# that matches how the stdlib was actually built.
SWIFT_TARGET_NAME = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7-unknown-linux-gnueabi${ARMPKGSFX_EABI}', '${TARGET_ARCH}-unknown-linux-gnu', d)}"
SWIFT_TARGET_ARCH = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7', '${TARGET_ARCH}', d)}"
TARGET_CPU_NAME = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7-a', '${TARGET_ARCH}', d)}"

BUILD_MODE = "${@['release', 'debug'][d.getVar('DEBUG_BUILD') == '1']}"

inherit swift-target-tune

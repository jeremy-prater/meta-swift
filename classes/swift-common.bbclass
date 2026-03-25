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

SWIFT_TARGET_NAME = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7-unknown-linux-gnueabihf', '${TARGET_ARCH}-unknown-linux-gnu', d)}"
SWIFT_TARGET_ARCH = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7', '${TARGET_ARCH}', d)}"
TARGET_CPU_NAME = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7-a', '${TARGET_ARCH}', d)}"

BUILD_MODE = "${@['release', 'debug'][d.getVar('DEBUG_BUILD') == '1']}"

inherit swift-target-tune

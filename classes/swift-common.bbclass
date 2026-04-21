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

# Selects the C++ runtime Swift recipes build and link against on Linux.
#   "gnu"  → GCC's libstdc++ (default; matches the rest of the OE distro)
#   "llvm" → clang's libc++ (OE-core's libcxx recipe)
# Swift-local knob, deliberately not OE-core's TC_CXX_RUNTIME (whole-toolchain
# switch that also rebuilds compiler-rt/libunwind/libgcc).
SWIFT_CXX_RUNTIME ?= "gnu"

SWIFT_TARGET_NAME = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7-unknown-linux-gnueabihf', '${TARGET_ARCH}-unknown-linux-gnu', d)}"
SWIFT_TARGET_ARCH = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7', '${TARGET_ARCH}', d)}"
TARGET_CPU_NAME = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7-a', '${TARGET_ARCH}', d)}"

BUILD_MODE = "${@['release', 'debug'][d.getVar('DEBUG_BUILD') == '1']}"

# Under libc++, swiftc's Clang importer needs Clang's builtin headers (stddef.h
# etc.) reachable so libc++'s <stddef.h> #include_next chain resolves. swiftc
# passes -resource-dir=<swift-resource-dir> to Clang, which then looks for
# builtins under <swift-resource-dir>/clang/include -- a path that doesn't exist
# in OE's split Swift/Clang layout. Bridge it with a symlink into the native
# Clang install. libstdc++ doesn't trigger the same module-build chain, so skip.
do_fix_swift_clang_resource_dir() {
    if [ "${SWIFT_CXX_RUNTIME}" = "llvm" ]; then
        install -d ${STAGING_DIR_TARGET}${libdir}/swift
        ln -sfn ${STAGING_DIR_NATIVE}/usr/lib/clang/${SWIFT_CLANG_VERSION} \
                ${STAGING_DIR_TARGET}${libdir}/swift/clang
    fi
}
addtask fix_swift_clang_resource_dir before do_configure after do_prepare_recipe_sysroot

inherit swift-target-tune

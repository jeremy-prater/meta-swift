# avoid conflicts with meta-clang
TOOLCHAIN = "gcc"

DEPENDS:append = " virtual/swift-native glibc gcc libgcc"
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

# Use Yocto's TARGET_SYS instead of the upstream Swift triple, so swiftc -target,
# .swiftinterface module names and clang's per-triple header lookup all agree.
#
# On 32-bit arm, TARGET_SYS is e.g. arm-oe-linux-gnueabi, which is wrong for
# swiftc's -target in two ways. First, the bare "arm" arch token isn't a
# platform swiftc recognises, so it warns "unknown platform, assuming
# -mfloat-abi=soft" and looks for the per-arch SwiftGlibc modulemap under an
# "arm" dir while the stdlib stages it under "armv7" (SWIFT_TARGET_ARCH), so the
# build dies with "error: no such module 'SwiftGlibc'". Second, Yocto keeps the
# float ABI out of the triple and conveys hard-float via -mfloat-abi=hard in
# TARGET_CC_ARCH. Those flags only reach the Clang importer via -Xcc, but
# swiftc's own codegen takes the float ABI from the triple, so a gnueabi triple
# is soft-float against a hard-float (cortexa15t2hf) sysroot. Promote the arch
# token to armv7 and append the "hf" environment suffix, keeping Yocto's
# vendor/os so .swiftinterface module names still agree.
#
# Non-arm triples additionally gain an explicit "-gnu" environment when Yocto's
# TARGET_OS is bare "linux" (i.e. glibc): Swift Build's platform registry
# refuses environment-less Linux triples outright --
#     unable to find a single platform name for triple 'aarch64-oe-linux'
# -- while accepting aarch64-oe-linux-gnu (the "oe" vendor is fine; only the
# missing environment matters). llbuild doesn't care either way, and clang and
# swiftc treat the two spellings identically, so this is harmless under the
# native build system and a hard prerequisite for ever adopting swiftbuild.
# armv7 already carries "hf" (gnueabihf) via the branch above, and musl
# targets already have "-musl" in TARGET_SYS, so only bare-"linux" needs it.
#
# NB SWIFT_TARGET_NAME is also the *_MODULE_TRIPLE for every runtime recipe:
# changing it renames the staged swiftmodule dirs (aarch64-oe-linux.swiftmodule
# -> aarch64-oe-linux-gnu.swiftmodule), so the Swift runtime rebuilds once on
# upgrade past this commit.
def swift_target_name(d):
    ts = d.getVar('TARGET_SYS')
    if d.getVar('TARGET_ARCH') == 'arm':
        return ts.replace('arm-', 'armv7-', 1) + 'hf'
    if d.getVar('TARGET_OS') == 'linux':
        return ts + '-gnu'
    return ts

SWIFT_TARGET_NAME = "${@swift_target_name(d)}"
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

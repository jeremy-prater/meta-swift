inherit cmake
inherit swift-common

python () {
    # Determine SWIFT_GCC_VERSION by examining bitbake's context dictionary key
    # RECIPE_MAINTAINER:pn-gcc-source-<version>
    import shlex

    gcc_src_maint_pkg = [x for x in d if x.startswith("RECIPE_MAINTAINER:pn-gcc-source-")][0]
    gcc_ver = gcc_src_maint_pkg.rpartition("-")[2]

    d.setVar("SWIFT_GCC_VERSION", gcc_ver)

    def expand_swiftc_cc_flags(flags):
        flags = [['-Xcc', flag] for flag in flags]
        return sum(flags, [])

    def concat_flags(flags):
        return " ".join(flags)

    # ensure target-specific tune CC flags are propagated to clang and swiftc.
    # Note we are not doing this at present for LD flags, as there are none in
    # the architectures we support (and it would make the string expansion more
    # complicated).
    target_cc_arch = shlex.split(d.getVar("TARGET_CC_ARCH"))

    d.setVar("SWIFT_EXTRA_SWIFTC_CC_FLAGS", concat_flags(expand_swiftc_cc_flags(target_cc_arch)))
}

EXTRA_INCLUDE_FLAGS ?= "\
    -I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_GCC_VERSION}/${TARGET_SYS} \
    -I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_GCC_VERSION} \
    -I${STAGING_DIR_TARGET}"

# not supported by clang
DEBUG_PREFIX_MAP:remove = "-fcanon-prefix-map"

HOST_CC_ARCH:prepend = "-target ${SWIFT_TARGET_NAME} "

################################################################################
# NOTE: The host running bitbake must have lld available and the following     #
# must be added to the local.conf file:                                        #
#                                                                              #
# HOSTTOOLS:append = " ld.lld"                                                 #
#                                                                              #
################################################################################

# Add build-id to generated binaries
TARGET_LDFLAGS:append = " -Xlinker --build-id=sha1"

# Use Apple's provided clang (it understands Apple's custom compiler flags)
# Made available via swift-native package.
OECMAKE_C_COMPILER = "clang"
OECMAKE_CXX_COMPILER = "clang++"

# Point clang to where the C++ runtime is for our target arch
RUNTIME_FLAGS = "${TARGET_CC_ARCH} -w -fuse-ld=lld -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION}"
TARGET_LDFLAGS:append = " ${TARGET_LD_ARCH} -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION}"

# Remove unsupported linker flags
TARGET_LDFLAGS:remove = "-Wl,-O1"
TARGET_LDFLAGS:remove = "-Wl,--hash-style=gnu"
TARGET_LDFLAGS:remove = "-Wl,--as-needed"

# Disable prefix map since we're not using GCC
DEBUG_PREFIX_MAP = ""

OECMAKE_C_FLAGS:append = " ${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"
OECMAKE_CXX_FLAGS:append = " ${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"
OECMAKE_ASM_FLAGS:append = " ${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"

SWIFTC_BIN = "${STAGING_DIR_NATIVE}/usr/bin/swiftc"

EXTRA_OECMAKE:append = " -DCMAKE_Swift_COMPILER=${SWIFTC_BIN}"
EXTRA_OECMAKE:append = " -DCMAKE_SWIFT_COMPILER=${SWIFTC_BIN}"
EXTRA_OECMAKE:append = ' -DCMAKE_Swift_FLAGS="${SWIFT_FLAGS}"'
EXTRA_OECMAKE:append = " -DSWIFT_USE_LINKER=lld"
EXTRA_OECMAKE:append = " -DLLVM_USE_LINKER=lld"
EXTRA_OECMAKE:append = " -DLLVM_DIR=${HOST_LLVM_PATH}/cmake/llvm"
EXTRA_OECMAKE:append = " -DLLVM_BUILD_LIBRARY_DIR=${HOST_LLVM_PATH}"

# Additional parameters to pass to swiftc
EXTRA_SWIFTC_FLAGS ??= ""

SWIFT_FLAGS = "-target ${SWIFT_TARGET_NAME} -use-ld=lld \
    -resource-dir ${STAGING_DIR_TARGET}/usr/lib/swift \
    -module-cache-path ${B}/${BUILD_MODE}/ModuleCache \
    -Xclang-linker -B${STAGING_DIR_TARGET}/usr/lib \
    -Xclang-linker -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION} \
    ${SWIFT_EXTRA_SWIFTC_CC_FLAGS} \
    -Xcc -I${STAGING_DIR_NATIVE}/usr/lib/${TARGET_SYS}/gcc/${TARGET_SYS}/${SWIFT_GCC_VERSION}/include \
    -Xcc -I${STAGING_DIR_NATIVE}/usr/lib/${TARGET_SYS}/gcc/${TARGET_SYS}/${SWIFT_GCC_VERSION}/include-fixed \
    -L${STAGING_DIR_TARGET} \
    -L${STAGING_DIR_TARGET}/lib \
    -L${STAGING_DIR_TARGET}/usr/lib \
    -L${STAGING_DIR_TARGET}/usr/lib/swift \
    -L${STAGING_DIR_TARGET}/usr/lib/swift/linux \
    -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION} \
    -sdk ${STAGING_DIR_TARGET} \
    ${EXTRA_SWIFTC_FLAGS} \
"

HOST_LLVM_PATH = "${STAGING_DIR_NATIVE}/usr/lib"

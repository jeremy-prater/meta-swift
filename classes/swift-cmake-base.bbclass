inherit cmake

# avoid conflicts with meta-clang
TOOLCHAIN = "gcc"

DEPENDS:append = " swift-native libgcc gcc glibc "

SWIFT_TARGET_ARCH = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7', 'aarch64', d)}"
SWIFT_TARGET_NAME = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7-unknown-linux-gnueabihf', 'aarch64-unknown-linux-gnu', d)}"
TARGET_CPU_NAME = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7-a', 'aarch64', d)}"

SWIFT_GCC_VERSION = "13.3.0"

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

# Use lld (see note above)
TARGET_LDFLAGS:append = " -fuse-ld=lld"

# Add build-id to generated binaries
TARGET_LDFLAGS:append = " -Xlinker --build-id=sha1"

# Use Apple's provided clang (it understands Apple's custom compiler flags)
# Made available via swift-native package.
OECMAKE_C_COMPILER = "clang"
OECMAKE_CXX_COMPILER = "clang++"

# Point clang to where the C++ runtime is for our target arch
RUNTIME_FLAGS = "-w -fuse-ld=lld -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION}"
TARGET_LDFLAGS:append = " -w -fuse-ld=lld -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION}"

OECMAKE_C_FLAGS:append = " ${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"
OECMAKE_CXX_FLAGS:append = " ${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"
OECMAKE_ASM_FLAGS:append = " ${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"

SWIFTC_BIN = "${STAGING_DIR_NATIVE}/usr/bin/swiftc"

EXTRA_OECMAKE:append = " -DCMAKE_Swift_COMPILER=${SWIFTC_BIN}"
EXTRA_OECMAKE:append = " -DCMAKE_SWIFT_COMPILER=${SWIFTC_BIN}"

BUILD_MODE = "${@['release', 'debug'][d.getVar('DEBUG_BUILD') == '1']}"

# Additional parameters to pass to swiftc
EXTRA_SWIFTC_FLAGS ??= ""

SWIFT_FLAGS = "-target ${SWIFT_TARGET_NAME} -use-ld=lld \
    -resource-dir ${STAGING_DIR_TARGET}/usr/lib/swift \
    -module-cache-path ${B}/${BUILD_MODE}/ModuleCache \
    -Xclang-linker -B${STAGING_DIR_TARGET}/usr/lib \
    -Xclang-linker -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION} \
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

EXTRA_OECMAKE:append = ' -DCMAKE_Swift_FLAGS="${SWIFT_FLAGS}"'
EXTRA_OECMAKE:append = " -DSWIFT_USE_LINKER=lld"
EXTRA_OECMAKE:append = " -DLLVM_USE_LINKER=lld"
EXTRA_OECMAKE:append = " -DLLVM_DIR=${HOST_LLVM_PATH}/cmake/llvm"
EXTRA_OECMAKE:append = " -DLLVM_BUILD_LIBRARY_DIR=${HOST_LLVM_PATH}"

EXTRANATIVEPATH:append = " swift-tools"

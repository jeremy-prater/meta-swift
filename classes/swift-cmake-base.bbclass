inherit cmake

DEPENDS_append += " swift-native libgcc gcc glibc "

HOST_CC_ARCH_prepend = "-target armv7-unknown-linux-gnueabih"

################################################################################
# NOTE: The host running bitbake must have lld available and the following     #
# must be added to the local.conf file:                                        #
#                                                                              #
# HOSTTOOLS += "ld.lld lld"                                                    #
#                                                                              #
################################################################################

# Use lld (see note above)
TARGET_LDFLAGS += "-fuse-ld=lld"

# Use Apple's provided clang (it understands Apple's custom compiler flags)
# Made available via swift-native package.
OECMAKE_C_COMPILER = "clang"
OECMAKE_CXX_COMPILER = "clang++"

export GCC_VERSION = "`basename ${STAGING_DIR_TARGET}/usr/include/c++/*`"

# Point clang to where the C++ runtime is for our target arch
RUNTIME_FLAGS = "-B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${GCC_VERSION}"
TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${GCC_VERSION}"

EXTRA_INCLUDE_FLAGS ?= ""
OECMAKE_C_FLAGS += "${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"
OECMAKE_CXX_FLAGS += "${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"

# Additional parameters to pass to swiftc
EXTRA_SWIFTC_FLAGS ??= ""

SWIFT_FLAGS = "-target armv7-unknown-linux-gnueabihf -use-ld=lld \
-resource-dir ${STAGING_DIR_TARGET}/usr/lib/swift \
-module-cache-path ${B}/ModuleCache \
-Xclang-linker -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${GCC_VERSION} \
-Xclang-linker -B${STAGING_DIR_TARGET}/usr/lib \
-Xcc -I${STAGING_DIR_NATIVE}/usr/lib/arm-poky-linux-gnueabi/gcc/arm-poky-linux-gnueabi/${GCC_VERSION}/include \
-Xcc -I${STAGING_DIR_NATIVE}/usr/lib/arm-poky-linux-gnueabi/gcc/arm-poky-linux-gnueabi/${GCC_VERSION}/include-fixed \
-L${STAGING_DIR_TARGET} \
-L${STAGING_DIR_TARGET}/lib \
-L${STAGING_DIR_TARGET}/usr/lib \
-L${STAGING_DIR_TARGET}/usr/lib/swift \
-L${STAGING_DIR_TARGET}/usr/lib/swift/linux \
-L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${GCC_VERSION} \
-sdk ${STAGING_DIR_TARGET} \
${EXTRA_SWIFTC_FLAGS} \
"

EXTRA_OECMAKE += '-DCMAKE_Swift_FLAGS="${SWIFT_FLAGS}"'


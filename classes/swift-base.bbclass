
inherit cmake

DEPENDS += "swift-native"

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

# Point clang to where the C++ runtime is for our target arch
RUNTIME_FLAGS = "-B${WORKDIR}/recipe-sysroot/usr/lib/${TARGET_SYS}/9.3.0"
TARGET_LDFLAGS += "-L${WORKDIR}/recipe-sysroot/usr/lib/${TARGET_SYS}/9.3.0"

EXTRA_INCLUDE_FLAGS ?= ""
OECMAKE_C_FLAGS += "${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"
OECMAKE_CXX_FLAGS += "${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"

# Additional parameters to pass to swiftc
EXTRA_SWIFTC_FLAGS ??= ""

SWIFT_FLAGS = "-target armv7-unknown-linux-gnueabihf -use-ld=lld \
-resource-dir ${WORKDIR}/recipe-sysroot/usr/lib/swift \
-Xclang-linker -B${WORKDIR}/recipe-sysroot/usr/lib/${TARGET_SYS}/9.3.0 \
-Xclang-linker -B${WORKDIR}/recipe-sysroot/usr/lib \
-Xcc -I${WORKDIR}/recipe-sysroot-native/usr/lib/arm-poky-linux-gnueabi/gcc/arm-poky-linux-gnueabi/9.3.0/include \
-Xcc -I${WORKDIR}/recipe-sysroot-native/usr/lib/arm-poky-linux-gnueabi/gcc/arm-poky-linux-gnueabi/9.3.0/include-fixed \
-L${WORKDIR}/recipe-sysroot/usr/lib/${TARGET_SYS}/9.3.0 \
-L${WORKDIR}/recipe-sysroot/lib \
-L${WORKDIR}/recipe-sysroot \
-L${STAGING_DIR_TARGET}/lib \
-L${STAGING_DIR_TARGET}/usr/lib \
-L${WORKDIR}/recipe-sysroot/usr/lib \
-L${WORKDIR}/recipe-sysroot/usr/lib/swift \
-L${WORKDIR}/recipe-sysroot/usr/lib/swift/linux \
-sdk ${WORKDIR}/recipe-sysroot \
${EXTRA_SWIFTC_FLAGS} \
"

EXTRA_OECMAKE += '-DCMAKE_Swift_FLAGS="${SWIFT_FLAGS}"'

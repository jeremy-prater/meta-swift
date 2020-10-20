SUMMARY = "The Foundation framework defines a base layer of functionality that is required for almost all applications."
HOMEPAGE = "https://github.com/apple/swift-corelibs-foundation"

LICENSE = "Apache-2.0" 
LIC_FILES_CHKSUM = "file://LICENSE;md5=1cd73afe3fb82e8d5c899b9d926451d0"

PV = "5.3"

SRC_URI = "git://github.com/apple/swift-corelibs-foundation;branch=${SRCBRANCH}"
SRCBRANCH = "release/${PV}"
SRCREV = "dfb10f7f74b73ba5742f3defcbb4d011abe9f2d4"

S = "${WORKDIR}/git"

DEPENDS = "swift-test-native libgcc gcc glibc ncurses swift-stdlib libdispatch libxml2 icu"

inherit cmake

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
OECMAKE_C_FLAGS += "-B${WORKDIR}/recipe-sysroot/usr/lib/${TARGET_SYS}/9.3.0"
OECMAKE_CXX_FLAGS += "-B${WORKDIR}/recipe-sysroot/usr/lib/${TARGET_SYS}/9.3.0"
TARGET_LDFLAGS += "-L${WORKDIR}/recipe-sysroot/usr/lib/${TARGET_SYS}/9.3.0"

TARGET_LDFLAGS += "-L${WORKDIR}/recipe-sysroot/usr/lib/swift/linux"

# Enable Swift parts
EXTRA_OECMAKE += "-DENABLE_SWIFT=YES"

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
-v \
"

EXTRA_OECMAKE += '-DCMAKE_Swift_FLAGS="${SWIFT_FLAGS}"'

EXTRA_OECMAKE+= "-Ddispatch_DIR=${WORKDIR}/recipe-sysroot/usr/lib/swift/dispatch"

# Ensure the right CPU is targeted
TARGET_CPU_NAME = "armv7-a"
cmake_do_generate_toolchain_file_append() {
    sed -i 's/set([ ]*CMAKE_SYSTEM_PROCESSOR .*[ ]*)/set(CMAKE_SYSTEM_PROCESSOR ${TARGET_CPU_NAME})/' ${WORKDIR}/toolchain.cmake
}


SUMMARY = "Libdispatch recipe"
HOMEPAGE = "https://github.com/apple/swift-corelibs-libdispatch"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=1cd73afe3fb82e8d5c899b9d926451d0"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_URI = "git://github.com/apple/swift-corelibs-libdispatch.git;branch=${SRCBRANCH} \
           file://0001-Silence-implicit-int-float-conversion-unused-result.patch \
           file://0001-Ensure-swift-support-is-turned-on.patch \
           file://0001-Make-dispatchConfig.cmake-not-depend-on-build-dir.patch \
           "
SRCBRANCH = "release/${PV}"
SRCREV = "25ea083a3af4ca09eee2b6dbdf58f1b163f87008"

DEPENDS = "swift-native libgcc gcc glibc ncurses swift-stdlib"

S = "${WORKDIR}/git"

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
OECMAKE_C_FLAGS += "-B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/9.3.0"
OECMAKE_CXX_FLAGS += "-B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/9.3.0"
TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/9.3.0"

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux"

# Enable Swift parts
EXTRA_OECMAKE += "-DENABLE_SWIFT=YES"

SWIFT_FLAGS = "-target armv7-unknown-linux-gnueabihf -use-ld=lld \
-resource-dir ${STAGING_DIR_TARGET}/usr/lib/swift \
-Xclang-linker -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/9.3.0 \
-Xclang-linker -B${STAGING_DIR_TARGET}/usr/lib \
-Xcc -I${STAGING_DIR_NATIVE}/usr/lib/arm-poky-linux-gnueabi/gcc/arm-poky-linux-gnueabi/9.3.0/include \
-Xcc -I${STAGING_DIR_NATIVE}/usr/lib/arm-poky-linux-gnueabi/gcc/arm-poky-linux-gnueabi/9.3.0/include-fixed \
-L${STAGING_DIR_TARGET} \
-L${STAGING_DIR_TARGET}/lib \
-L${STAGING_DIR_TARGET}/usr/lib \
-L${STAGING_DIR_TARGET}/usr/lib/swift \
-L${STAGING_DIR_TARGET}/usr/lib/swift/linux \
-L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/9.3.0 \
-sdk ${STAGING_DIR_TARGET} \
-v \
"

EXTRA_OECMAKE += '-DCMAKE_Swift_FLAGS="${SWIFT_FLAGS}"'

# Ensure the right CPU is targeted
TARGET_CPU_NAME = "armv7-a"
cmake_do_generate_toolchain_file_append() {
    sed -i 's/set([ ]*CMAKE_SYSTEM_PROCESSOR .*[ ]*)/set(CMAKE_SYSTEM_PROCESSOR ${TARGET_CPU_NAME})/' ${WORKDIR}/toolchain.cmake
}

FILES_${PN} = "\
    ${libdir}/swift/linux/libswiftDispatch.so \
    ${libdir}/swift/linux/libBlocksRuntime.so \
    ${libdir}/swift/linux/libdispatch.so \
"

# TODO: these are installed into ${libdir}, but that seems wrong...
FILES_${PN}-dev = "\
    ${libdir}/swift/dispatch/cmake/dispatchConfig.cmake \
    ${libdir}/swift/dispatch/cmake/dispatchExports.cmake \
    ${libdir}/swift/dispatch/cmake/dispatchExports-noconfig.cmake \
    ${libdir}/swift/dispatch/module.modulemap \
    ${libdir}/swift/dispatch/introspection.h \
    ${libdir}/swift/dispatch/semaphore.h \
    ${libdir}/swift/dispatch/dispatch.h \
    ${libdir}/swift/dispatch/block.h \
    ${libdir}/swift/dispatch/base.h \
    ${libdir}/swift/dispatch/object.h \
    ${libdir}/swift/dispatch/group.h \
    ${libdir}/swift/dispatch/data.h \
    ${libdir}/swift/dispatch/io.h \
    ${libdir}/swift/dispatch/queue.h \
    ${libdir}/swift/dispatch/source.h \
    ${libdir}/swift/dispatch/time.h \
    ${libdir}/swift/dispatch/once.h \
    ${libdir}/swift/os/object.h \
    ${libdir}/swift/os/generic_win_base.h \
    ${libdir}/swift/os/generic_unix_base.h \
    ${libdir}/swift/Block/Block.h \
    ${libdir}/swift/linux/armv7/Dispatch.swiftmodule \
"

FILES_${PN}-doc = "\
    ${libdir}/swift/linux/armv7/Dispatch.swiftdoc \
"

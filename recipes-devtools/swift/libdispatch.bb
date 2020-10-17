
SUMMARY = "Libdispatch recipe"
HOMEPAGE = "https://github.com/apple/swift-corelibs-libdispatch"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=1cd73afe3fb82e8d5c899b9d926451d0"

SRC_URI = "https://github.com/apple/swift-corelibs-libdispatch/archive/swift-5.3-RELEASE.tar.gz \
           file://0001-Silence-implicit-int-float-conversion-unused-result.patch \
           file://0001-Ensure-swift-support-is-turned-on.patch \
           "
SRC_URI[sha256sum] = "6805b555aab65d740fccaa99570fd29b32efa6c310fd42524913e44509dc4969"

DEPENDS = "swift-test-native libgcc gcc glibc ncurses swift-stdlib"

S = "${WORKDIR}/swift-corelibs-libdispatch-swift-5.3-RELEASE"

inherit cmake

HOST_CC_ARCH_prepend = "-target armv7-unknown-linux-gnueabih"

# Required in local.conf:
#HOSTTOOLS += "ld.lld lld"
# Use lld
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

#OECMAKE_C_FLAGS += "-I${WORKDIR}/recipe-sysroot/usr/include/linux"
#OECMAKE_CXX_FLAGS += "-I${WORKDIR}/recipe-sysroot/usr/include/linux"

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
    ${libdir}/swift/dispatch/introspection.h \
    ${libdir}/swift/dispatch/semaphore.h \
    ${libdir}/swift/dispatch/dispatch.h \
    ${libdir}/swift/dispatch/block.h \
    ${libdir}/swift/dispatch/base.h \
    ${libdir}/swift/dispatch/module.modulemap \
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

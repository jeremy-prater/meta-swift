
SUMMARY = "Libdispatch recipe"
HOMEPAGE = "https://github.com/apple/swift-corelibs-libdispatch"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=1cd73afe3fb82e8d5c899b9d926451d0"

SRC_URI = "https://github.com/apple/swift-corelibs-libdispatch/archive/swift-5.3-RELEASE.tar.gz \
           file://0001-Silence-implicit-int-float-conversion-unused-result.patch \
           file://0001-Ensure-swift-support-is-turned-on.patch \
           "
SRC_URI[sha256sum] = "6805b555aab65d740fccaa99570fd29b32efa6c310fd42524913e44509dc4969"

DEPENDS = "swift-test-native libgcc gcc glibc ncurses"

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

# Enable Swift parts
EXTRA_OECMAKE += "-DENABLE_SWIFT=YES"

SWIFT_FLAGS = "-target armv7-unknown-linux-gnueabihf -use-ld=lld -L${STAGING_DIR_TARGET}/lib -L${STAGING_DIR_TARGET}/usr/lib -lstdc++"
#EXTRA_OECMAKE += '-DCMAKE_Swift_FLAGS="${SWIFT_FLAGS} -sdk ${WORKDIR}/recipe-sysroot -L${WORKDIR}/recipe-sysroot"'
EXTRA_OECMAKE += "-DCMAKE_SYSTEM_PROCESSOR=armv7-a"
EXTRA_OECMAKE += '-DCMAKE_Swift_FLAGS="${SWIFT_FLAGS}"'

#export EXTRA_OECMAKE
#export CMAKE_Swift_FLAGS= "-sdk ${WORKDIR}/recipe-sysroot -target armv7-unknown-linux-gnueabih"

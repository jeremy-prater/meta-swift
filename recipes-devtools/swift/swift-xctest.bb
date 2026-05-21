SUMMARY = "swift-xctest"
DESCRIPTION = "A common framework for writing unit tests in Swift."
HOMEPAGE = "https://github.com/swiftlang/swift-corelibs-xctest"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1cd73afe3fb82e8d5c899b9d926451d0"

require swift-version.inc
PV = "${SWIFT_VERSION}+git${SRCPV}"

SRC_URI = "git://github.com/swiftlang/swift-corelibs-xctest.git;protocol=https;tag=${SWIFT_TAG};nobranch=1"

DEPENDS = "swift-stdlib swift-native libdispatch swift-foundation"
RDEPENDS:${PN} += "swift-stdlib libdispatch swift-foundation"

inherit swift-cmake-base

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}${libdir}/swift/linux"

EXTRA_OECMAKE += "-DBUILD_SHARED_LIBS=YES"
EXTRA_OECMAKE += "-DENABLE_TESTING=NO"
EXTRA_OECMAKE += "-DXCTest_MODULE_TRIPLE=${SWIFT_TARGET_NAME}"
EXTRA_OECMAKE += "-DCMAKE_FIND_ROOT_PATH:PATH=${CROSS_COMPILE_DEPS_PATH}"

# Ensure the right CPU is targeted (same as swift-foundation.bb)
cmake_do_generate_toolchain_file:append() {
    sed -i 's/set([ ]*CMAKE_SYSTEM_PROCESSOR .*[ ]*)/set(CMAKE_SYSTEM_PROCESSOR ${TARGET_CPU_NAME})/' ${WORKDIR}/toolchain.cmake
}

FILES:${PN} = "${libdir}/swift/linux/libXCTest.so"
FILES:${PN}-dev = "${libdir}/swift/linux/XCTest.swiftmodule/*"

INSANE_SKIP:${PN} = "buildpaths"
INSANE_SKIP:${PN}-dbg = "buildpaths"

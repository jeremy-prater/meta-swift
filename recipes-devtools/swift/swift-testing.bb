SUMMARY = "swift-testing"
DESCRIPTION = "A package with expressive and intuitive APIs that make testing your Swift code a breeze."
HOMEPAGE = "https://github.com/swiftlang/swift-testing"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=9426349f482bb39d6a4a080793545176"

require swift-version.inc
PV = "${SWIFT_VERSION}+git${SRCPV}"
SRCREV_FORMAT = "swift_testing"

SRC_URI = "git://github.com/swiftlang/swift-testing.git;protocol=https;tag=${SWIFT_TAG};nobranch=1"

DEPENDS = "swift-stdlib libdispatch swift-foundation"
RDEPENDS:${PN} += "swift-stdlib libdispatch swift-foundation"

inherit swift-cmake-base

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}${libdir}/swift/linux"

EXTRA_OECMAKE += "-DBUILD_SHARED_LIBS=YES"
EXTRA_OECMAKE += "-DSwiftTesting_MODULE_TRIPLE=${SWIFT_TARGET_NAME}"
EXTRA_OECMAKE += "-DSwiftTesting_MACRO=${STAGING_DIR_NATIVE}/usr/lib/swift/host/plugins/libTestingMacros.so"
EXTRA_OECMAKE += "-DCMAKE_FIND_ROOT_PATH:PATH=${CROSS_COMPILE_DEPS_PATH}"

# Ensure the right CPU is targeted (same as swift-foundation.bb)
cmake_do_generate_toolchain_file:append() {
    sed -i 's/set([ ]*CMAKE_SYSTEM_PROCESSOR .*[ ]*)/set(CMAKE_SYSTEM_PROCESSOR ${TARGET_CPU_NAME})/' ${WORKDIR}/toolchain.cmake
}

FILES:${PN} = "\
    ${libdir}/swift/linux/libTesting.so \
    ${libdir}/swift/linux/lib_TestingInterop.so \
    ${libdir}/swift/linux/lib_Testing_Foundation.so \
"
FILES:${PN}-dev = "\
    ${libdir}/swift/linux/Testing.swiftmodule/* \
    ${libdir}/swift/linux/_TestingInterop.swiftmodule/* \
    ${libdir}/swift/linux/_TestDiscovery.swiftmodule/* \
    ${libdir}/swift/linux/_Testing_Foundation.swiftmodule/* \
    ${libdir}/swift/linux/Testing.swiftcrossimport/* \
"
FILES:${PN}-staticdev = "\
    ${libdir}/swift/linux/lib_TestDiscovery.a \
"

INSANE_SKIP:${PN} = "buildpaths"
INSANE_SKIP:${PN}-dbg = "buildpaths"

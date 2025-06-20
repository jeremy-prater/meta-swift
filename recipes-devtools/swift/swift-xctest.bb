SUMMARY = "swift-xctest"
DESCRIPTION = "A common framework for writing unit tests in Swift."
HOMEPAGE = "https://github.com/swiftlang/swift-corelibs-xctest"

SWIFT_BUILD_TESTS = "0"

LICENSE = "Apache-2.0" 
LIC_FILES_CHKSUM = "file://LICENSE;md5=1cd73afe3fb82e8d5c899b9d926451d0"

require swift-version.inc
PV = "${SWIFT_VERSION}+git${SRCPV}"

SRC_URI = "git://github.com/swiftlang/swift-corelibs-xctest.git;protocol=https;tag=swift-${SWIFT_VERSION}-RELEASE;nobranch=1"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

inherit swift

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux"

do_install() {
    install -d ${D}${libdir}/swift/linux

    install -m 0755 ${BUILD_DIR}/libXCTest.so ${D}${libdir}/swift/linux
    install -m 0644 ${BUILD_DIR}/Modules/XCTest.swiftmodule ${D}${libdir}/swift/linux
    install -m 0644 ${BUILD_DIR}/Modules/XCTest.swiftdoc ${D}${libdir}/swift/linux

    rm -f ${BUILD_DIR}/Modules/XCTest.swiftsourceinfo
}

FILES:${PN} = "\
    ${libdir}/swift/linux/libXCTest.so \
"

FILES:${PN}-dev = "\
    ${libdir}/swift/linux/XCTest.swiftmodule \
    ${libdir}/swift/linux/XCTest.swiftdoc \
"

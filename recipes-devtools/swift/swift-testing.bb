SUMMARY = "swift-testing"
DESCRIPTION = "A package with expressive and intuitive APIs that make testing your Swift code a breeze."
HOMEPAGE = "https://github.com/swiftlang/swift-testing"

SWIFT_BUILD_TESTS = "0"

LICENSE = "Apache-2.0" 
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=9426349f482bb39d6a4a080793545176"

require swift-version.inc
PV = "${SWIFT_VERSION}+git${SRCPV}"
SRCREV_FORMAT = "swift_testing"

SRC_URI = "git://github.com/swiftlang/swift-testing.git;protocol=https;tag=swift-${SWIFT_VERSION}-RELEASE;nobranch=1"
SRC_URI += "file://0001-build-as-dynamic-library.patch;striplevel=1;"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

inherit swift

do_install() {
    install -d ${D}${libdir}/swift/linux

    install -m 0644 ${BUILD_DIR}/libTesting.so ${D}${libdir}/swift/linux
    install -m 0644 ${BUILD_DIR}/Modules/Testing.swiftmodule ${D}${libdir}/swift/linux
    install -m 0644 ${BUILD_DIR}/Modules/Testing.swiftdoc ${D}${libdir}/swift/linux
    install -m 0644 ${BUILD_DIR}/Modules/Testing.swiftinterface ${D}${libdir}/swift/linux

    rm -f ${BUILD_DIR}/Modules/*.swiftsourceinfo
}

FILES:${PN} = "\
    ${libdir}/swift/linux/libTesting.so \
"

FILES:${PN}-dev = "\
    ${libdir}/swift/linux/Testing.swiftmodule \
    ${libdir}/swift/linux/Testing.swiftdoc \
    ${libdir}/swift/linux/Testing.swiftinterface \
"

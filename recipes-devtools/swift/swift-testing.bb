SUMMARY = "swift-testing"
DESCRIPTION = "A package with expressive and intuitive APIs that make testing your Swift code a breeze."
HOMEPAGE = "https://github.com/swiftlang/swift-testing"

LICENSE = "Apache-2.0" 
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=9426349f482bb39d6a4a080793545176"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_URI = "git://github.com/swiftlang/swift-testing.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

inherit swift

do_install() {
    install -d ${D}${libdir}/swift/linux

    install -m 0644 ${BUILD_DIR}/Modules/Testing.swiftmodule ${D}${libdir}/swift/linux
    install -m 0644 ${BUILD_DIR}/Modules/Testing.swiftdoc ${D}${libdir}/swift/linux

    rm -f ${BUILD_DIR}/Modules/*.swiftsourceinfo
}

FILES:${PN} = "\
"

FILES:${PN}-dev = "\
    ${libdir}/swift/linux/Testing.swiftmodule \
    ${libdir}/swift/linux/Testing.swiftdoc \
"

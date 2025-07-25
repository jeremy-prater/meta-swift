DESCRIPTION = "Swift Hello World!"
LICENSE = "CLOSED"

SWIFT_BUILD_TESTS = "1"

RDEPENDS:${PN} += "swift-xctest swift-testing"

SRC_URI = "\
    file://Package.swift \
    file://Sources \
    file://Tests \
"

S = "${SWIFT_UNPACKDIR}"
B = "${WORKDIR}/build"

inherit swift

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${BUILD_DIR}/hello-world ${D}${bindir}
    install -m 0755 ${BUILD_DIR}/hello-worldPackageTests.xctest ${D}${bindir}
}

INSANE_SKIP:${PN} = "buildpaths"
INSANE_SKIP:${PN}-dbg = "buildpaths"

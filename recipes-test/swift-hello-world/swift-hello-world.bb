DESCRIPTION = "Swift Hello World!"
LICENSE = "CLOSED"

SWIFT_BUILD_TESTS = "1"

SRC_URI = "\
    file://Package.swift \
    file://Sources \
    file://Tests \
"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

inherit swift

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${BUILD_DIR}/hello-world ${D}${bindir}
}

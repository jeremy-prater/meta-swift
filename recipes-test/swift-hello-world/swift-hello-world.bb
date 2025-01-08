DESCRIPTION = "Swift Hello World!"
LICENSE = "CLOSED"

SRC_URI = "\
    file://Sources \
    file://Package.swift \
"

inherit swift

do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${BUILD_DIR}/hello-world ${D}${bindir}
}

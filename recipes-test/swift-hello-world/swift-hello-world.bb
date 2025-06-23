DESCRIPTION = "swift 5.6.1 test application"
LICENSE = "CLOSED"

SRC_URI = "file://Sources/hello-world/main.swift \
           file://Package.swift \
"

inherit swift

do_install() {
    install -d ${D}${bindir}
    cp -rf ${B}/release/hello-world ${D}${bindir}/
}


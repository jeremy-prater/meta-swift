SUMMARY = "Swift toolchain for x86_64 linux"
HOMEPAGE = "https://swift.org/download/#releases"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/usr/share/swift/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_DIR = "swift-${PV}-RELEASE-ubuntu18.04"

SRC_URI = "https://swift.org/builds/swift-${PV}-release/ubuntu1804/swift-${PV}-RELEASE/${SRC_DIR}.tar.gz"
SRC_URI[sha256sum] = "5ac1fb9b8963e1c44f541f55cbf6cc10faefb1f21598d813f14f8aaeb22b1d80"

DEPENDS = "curl"

S = "${WORKDIR}/${SRC_DIR}"

inherit native

do_install_append () {
    install -d ${D}${bindir}/
    cp -r ${S}/usr/bin/* ${D}${bindir}

    install -d ${D}${libdir}/
    cp -r ${S}/usr/lib/* ${D}${libdir}

    install -d ${D}${includedir}/
    cp -r ${S}/usr/include/* ${D}${includedir}

    install -d ${D}${datadir}/
    cp -r ${S}/usr/share/* ${D}${datadir}
}


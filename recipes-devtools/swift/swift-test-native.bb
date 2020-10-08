SUMMARY = "Swift toolchain for x86_64 linux"
HOMEPAGE = "https://swift.org/download/#releases"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/usr/share/swift/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

SRC_URI = "https://swift.org/builds/swift-5.3-release/ubuntu1804/swift-5.3-RELEASE/swift-5.3-RELEASE-ubuntu18.04.tar.gz \
"
SRC_URI[sha256sum] = "5ac1fb9b8963e1c44f541f55cbf6cc10faefb1f21598d813f14f8aaeb22b1d80"

S = "${WORKDIR}/swift-5.3-RELEASE-ubuntu18.04"

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

#FILES_${PN} = "${prefix}/*"

inherit externalsrc
EXTERNALSRC="/home/kevin/Downloads/swift-5.3-RELEASE-ubuntu20.04"

SUMMARY = "Swift native toolchain for Linux"
HOMEPAGE = "https://swift.org/install/"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/usr/share/swift/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_DIR = "swift-${PV}-RELEASE-ubuntu24.04"
SRC_URI = "https://download.swift.org/swift-${PV}-release/ubuntu2404/swift-${PV}-RELEASE/swift-${PV}-RELEASE-ubuntu24.04.tar.gz"
SRC_URI[sha256sum] = "711d1d9e81c6d0dbc36202d9bc9bd491cc5fa79854fd0205ba27f40122b6ad5f"

DEPENDS = "curl"
RDEPENDS:${PN} = "ncurses-native"

S = "${WORKDIR}/${SRC_DIR}"

inherit native

########################################################################
# This informs bitbake that we want to install a non-default directory #
# in the native sysroot.                                               #
########################################################################

do_install:append () {
    install -d ${D}${bindir}
    cp -r ${S}/usr/bin/* ${D}${bindir}

    install -d ${D}${libdir}
    cp -rd ${S}/usr/lib/* ${D}${libdir}

    install -d ${D}${includedir}
    cp -rd ${S}/usr/include/* ${D}${includedir}

    install -d ${D}${datadir}
    cp -rd ${S}/usr/share/* ${D}${datadir}
}

FILES_${PN} += "${base_prefix}/*"

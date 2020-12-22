SUMMARY = "Swift toolchain for x86_64 linux"
HOMEPAGE = "https://swift.org/download/#releases"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/usr/share/swift/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "${SWIFT_VERSION}"

#######################################################################
# We use our own Swift 5.3 x86_64 Linux toolchain in order to link    #
# against the libgcc runtime in our ARM code.                         #
#######################################################################
SRC_DIR = "."
SRC_URI = "https://rpe-downloads-dev.s3-us-west-2.amazonaws.com/swift/swift-${PV}-ubuntu20.04-patched.tar.gz"
SRC_URI[sha256sum] = "47f9ff1fe255b0c40dadb8c66e356a6be75f55c34f3907e9840c8ff9da6ea5fe"


#######################################################################
# Once our own toolchain is no longer needed switch back to this src. #
#######################################################################
#SRC_DIR = "swift-${PV}-RELEASE-ubuntu18.04"
#SRC_URI = "https://swift.org/builds/swift-${PV}-release/ubuntu1804/swift-${PV}-RELEASE/${SRC_DIR}.tar.gz"
#SRC_URI[sha256sum] = "5ac1fb9b8963e1c44f541f55cbf6cc10faefb1f21598d813f14f8aaeb22b1d80"

DEPENDS = "curl"
RDEPENDS = "ncurses-native"

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


SUMMARY = "Swift toolchain for Linux"
HOMEPAGE = "https://swift.org/install/"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/usr/share/swift/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_DIR = "swift-${PV}-RELEASE-ubuntu24.04"
SRC_URI = "https://download.swift.org/swift-${PV}-release/ubuntu2404/swift-${PV}-RELEASE/swift-${PV}-RELEASE-ubuntu24.04.tar.gz"
SRC_URI[sha256sum] = "b014975844beac5ab3a7f71420181f45d0a93243f9ea4853e6588c05cad1e363"

DEPENDS = "curl"
RDEPENDS = "ncurses-native"

S = "${WORKDIR}/${SRC_DIR}"

inherit native


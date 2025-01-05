SUMMARY = "Swift native toolchain for Linux"
HOMEPAGE = "https://swift.org/install/"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/usr/share/swift/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "${SWIFT_VERSION}"

def swift_native_arch_suffix(d):
    host_arch = d.getVar('HOST_ARCH')
    if host_arch == 'x86_64':
        return ''
    else:
        return f'-{host_arch}'

def swift_native_arch_checksum(d):
    sha256 = {
      "x86_64": "57dbe6e85af6813b6013423d406e840bbb5ef14d7ef29eac1bfdb83d33e32c59",
      "aarch64": "XXX"
    }

    host_arch = d.getVar('HOST_ARCH')
    return sha256[host_arch]

SWIFT_ARCH_SUFFIX = "${@swift_native_arch_suffix(d)}"

SRC_DIR = "swift-${PV}-RELEASE-ubuntu24.04${SWIFT_ARCH_SUFFIX}"
SRC_URI = "https://download.swift.org/swift-${PV}-release/ubuntu2404${SWIFT_ARCH_SUFFIX}/swift-${PV}-RELEASE/swift-${PV}-RELEASE-ubuntu24.04${SWIFT_ARCH_SUFFIX}.tar.gz"
SRC_URI[sha256sum] = "${@swift_native_arch_checksum(d)}"

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

FILES:${PN} += "${base_prefix}/*"

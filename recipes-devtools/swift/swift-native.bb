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
      "x86_64": "d749d5fe2d6709ee988e96b16f02bca7b53304d09925e31063fd5ec56019de9f",
      "aarch64": "0be937ec11860cad109ab422541643f7c6b1156daa91c9e2c70d8f03ce245cb6"
    }

    host_arch = d.getVar('HOST_ARCH')
    return sha256[host_arch]

SWIFT_ARCH_SUFFIX = "${@swift_native_arch_suffix(d)}"

SRC_DIR = "swift-${SWIFT_VERSION}-RELEASE-ubuntu24.04${SWIFT_ARCH_SUFFIX}"
SRC_URI = "https://download.swift.org/swift-${SWIFT_VERSION}-release/ubuntu2404${SWIFT_ARCH_SUFFIX}/swift-${SWIFT_VERSION}-RELEASE/swift-${SWIFT_VERSION}-RELEASE-ubuntu24.04${SWIFT_ARCH_SUFFIX}.tar.gz"
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

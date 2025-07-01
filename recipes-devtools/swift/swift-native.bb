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
      "x86_64": "3f4b7e2c9219a52fcecb7cb90153f9aacd3da85aa53e75e38dd406c0e9122551",
      "aarch64": "9441091be33ca5d909337b8795f98e8234a52d79c197c6c015bca4b8994fbc87"
    }

    host_arch = d.getVar('HOST_ARCH')
    return sha256[host_arch]

SWIFT_ARCH_SUFFIX = "${@swift_native_arch_suffix(d)}"

SWIFT_LINUX_DISTRO = "amazonlinux2"

SRC_DIR = "${SWIFT_TAG}-${SWIFT_LINUX_DISTRO}${SWIFT_ARCH_SUFFIX}"
SRC_URI = "https://download.swift.org/swift-${SWIFT_VERSION}-release/${SWIFT_LINUX_DISTRO}${SWIFT_ARCH_SUFFIX}/${SWIFT_TAG}/${SWIFT_TAG}-${SWIFT_LINUX_DISTRO}${SWIFT_ARCH_SUFFIX}.tar.gz"
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

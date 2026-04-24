# Shared by swift-binary-native (deploys toolchain) and swift-bootstrap-native
# (private prefix for bootstrapping the source build).

require swift-version.inc
PV = "${SWIFT_VERSION}"

LICENSE = "Apache-2.0"

def swift_native_arch_suffix(d):
    host_arch = d.getVar('HOST_ARCH')
    if host_arch == 'x86_64':
        return ''
    else:
        return f'-{host_arch}'

def swift_host_arch(d):
    return swift_native_arch_suffix(d).lstrip('-')

def swift_native_arch_checksum(d):
    sha256 = {
      "x86_64": "1dfa4a000d7cd5565cae340be8feeb342f5c3caa735a0dec67391c2d2e1370c9",
      "aarch64": "c6847f4eb89cb6f4faae1695f7b63b276b26aae5fd57dc22ebf968b48c797857"
    }

    host_arch = d.getVar('HOST_ARCH')
    return sha256[host_arch]

SWIFT_ARCH_SUFFIX = "${@swift_native_arch_suffix(d)}"
SWIFT_HOST_ARCH = "${@swift_host_arch(d)}"

SWIFT_LINUX_DISTRO = "amazonlinux2"

SRC_DIR = "${SWIFT_TAG}-${SWIFT_LINUX_DISTRO}${SWIFT_ARCH_SUFFIX}"
SRC_URI = "https://download.swift.org/swift-${SWIFT_VERSION}-release/${SWIFT_LINUX_DISTRO}${SWIFT_ARCH_SUFFIX}/${SWIFT_TAG}/${SWIFT_TAG}-${SWIFT_LINUX_DISTRO}${SWIFT_ARCH_SUFFIX}.tar.gz"
SRC_URI[sha256sum] = "${@swift_native_arch_checksum(d)}"

S = "${UNPACKDIR}/${SRC_DIR}"

LIC_FILES_CHKSUM = "file://${S}/usr/share/swift/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

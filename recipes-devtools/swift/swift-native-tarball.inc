# Shared by swift-binary-native (deploys the toolchain into the native
# sysroot) and swift-bootstrap-native (stages it under a private prefix so
# the from-source swift-native can bootstrap against it).

require swift-version.inc
require swift-bundle-checksums.inc
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

SWIFT_ARCH_SUFFIX = "${@swift_native_arch_suffix(d)}"
SWIFT_HOST_ARCH = "${@swift_host_arch(d)}"

SWIFT_LINUX_DISTRO = "amazonlinux2"

SRC_DIR = "${SWIFT_TAG}-${SWIFT_LINUX_DISTRO}${SWIFT_ARCH_SUFFIX}"
SRC_URI = "https://download.swift.org/swift-${SWIFT_VERSION}-release/${SWIFT_LINUX_DISTRO}${SWIFT_ARCH_SUFFIX}/${SWIFT_TAG}/${SWIFT_TAG}-${SWIFT_LINUX_DISTRO}${SWIFT_ARCH_SUFFIX}.tar.gz"
SRC_URI[sha256sum] = "${@swift_bundle_checksum(d, d.getVar('HOST_ARCH'))}"

S = "${UNPACKDIR}/${SRC_DIR}"

LIC_FILES_CHKSUM = "file://${S}/usr/share/swift/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

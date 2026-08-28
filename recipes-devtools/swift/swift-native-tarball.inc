# The swift.org prebuilt host-toolchain bundle: URL scheme, per-arch suffix,
# unpack dir and licence bookkeeping in one place. Shared by
# swift-binary-native (deploys into the native sysroot), swift-bootstrap-native
# (stages under the private bootstrap prefix) and nativesdk-swift (ships the
# bundle in the SDK).
#
# The bundle is per-architecture. Native consumers pick it by the build host
# (HOST_ARCH, the default); nativesdk-swift overrides SWIFT_BUNDLE_ARCH to
# SDK_ARCH so the SDK carries the binary for the machine the SDK runs on.

require swift-version.inc
require swift-bundle-checksums.inc
PV = "${SWIFT_VERSION}"

LICENSE = "Apache-2.0"

SWIFT_BUNDLE_ARCH ?= "${HOST_ARCH}"

# swift.org names the x86_64 bundle with no arch suffix and every other arch
# with "-<arch>".
def swift_bundle_arch_suffix(d):
    arch = d.getVar('SWIFT_BUNDLE_ARCH')
    if arch == 'x86_64':
        return ''
    else:
        return f'-{arch}'

def swift_host_arch(d):
    return swift_bundle_arch_suffix(d).lstrip('-')

SWIFT_ARCH_SUFFIX = "${@swift_bundle_arch_suffix(d)}"
SWIFT_HOST_ARCH = "${@swift_host_arch(d)}"

SWIFT_LINUX_DISTRO = "ubuntu22.04"
SWIFT_LINUX_DISTRO_DIR = "${@d.getVar('SWIFT_LINUX_DISTRO').replace('.', '')}"

SRC_DIR = "${SWIFT_TAG}-${SWIFT_LINUX_DISTRO}${SWIFT_ARCH_SUFFIX}"
SRC_URI = "https://download.swift.org/${SWIFT_DOWNLOAD_DIR}/${SWIFT_LINUX_DISTRO_DIR}${SWIFT_ARCH_SUFFIX}/${SWIFT_TAG}/${SWIFT_TAG}-${SWIFT_LINUX_DISTRO}${SWIFT_ARCH_SUFFIX}.tar.gz"
SRC_URI[sha256sum] = "${@swift_bundle_checksum(d, d.getVar('SWIFT_BUNDLE_ARCH'))}"

S = "${UNPACKDIR}/${SRC_DIR}"

LIC_FILES_CHKSUM = "file://${S}/usr/share/swift/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

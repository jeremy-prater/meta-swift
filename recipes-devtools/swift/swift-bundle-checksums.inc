# SHA256 checksums for the swift.org prebuilt host toolchain bundles
# (download.swift.org, amazonlinux2). Shared by swift-native.bb (keyed on
# HOST_ARCH) and nativesdk-swift.bb (keyed on SDK_ARCH): both pull the same
# per-arch tarball, so the hashes live in one place. Update these together
# with SWIFT_VERSION in swift-version.inc whenever the pinned release changes.
#
# swift.org only publishes x86_64 and aarch64 host bundles.
def swift_bundle_checksum(d, arch):
    sha256 = {
        "x86_64":  "ece8c60b48b52e589f144462e247617b6ea0ce9f85dbd3d12907cf1821e20c20",
        "aarch64": "ea7258ee30ccf21e8d20a007c41156be997ab558954c5ab12915c7a4fbcbc89e",
    }
    if arch not in sha256:
        bb.fatal('swift.org bundle: no checksum for arch {}'.format(arch))
    return sha256[arch]

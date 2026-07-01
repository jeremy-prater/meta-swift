# SHA256 checksums for the swift.org prebuilt host toolchain bundles
# (download.swift.org, amazonlinux2). Shared by swift-native.bb (keyed on
# HOST_ARCH) and nativesdk-swift.bb (keyed on SDK_ARCH): both pull the same
# per-arch tarball, so the hashes live in one place. Update these together
# with SWIFT_VERSION in swift-version.inc whenever the pinned release changes.
#
# swift.org only publishes x86_64 and aarch64 host bundles.
def swift_bundle_checksum(d, arch):
    sha256 = {
        "x86_64":  "648daccc9062045cb42431f6c7d620858b729abccb6bb9075a6b990e6259e897",
        "aarch64": "8b85ce9e2a13802654e2a097d8720f0726d0900adc3579af1649190cc6cd49be",
    }
    if arch not in sha256:
        bb.fatal('swift.org bundle: no checksum for arch {}'.format(arch))
    return sha256[arch]

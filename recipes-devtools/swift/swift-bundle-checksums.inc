# SHA256 checksums for the swift.org prebuilt host toolchain bundles
# (download.swift.org, amazonlinux2). Shared by swift-native.bb (keyed on
# HOST_ARCH) and nativesdk-swift.bb (keyed on SDK_ARCH): both pull the same
# per-arch tarball, so the hashes live in one place. Update these together
# with SWIFT_VERSION in swift-version.inc whenever the pinned release changes.
#
# swift.org only publishes x86_64 and aarch64 host bundles.
def swift_bundle_checksum(d, arch):
    sha256 = {
        "x86_64":  "096e301d306d26d18fd2599e51b1875b46614243e38186c0d7ef37318d0f6f0a",
        "aarch64": "4acd67da1b8039bad01080a9fa85ec1150fb070b56cf53e6ea431bf0c552709c",
    }
    if arch not in sha256:
        bb.fatal('swift.org bundle: no checksum for arch {}'.format(arch))
    return sha256[arch]

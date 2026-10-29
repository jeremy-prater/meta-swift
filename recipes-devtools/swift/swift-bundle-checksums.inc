# SHA256 checksums for the swift.org prebuilt host toolchain bundles
# (download.swift.org, ubi9). Shared by swift-native.bb (keyed on
# HOST_ARCH) and nativesdk-swift.bb (keyed on SDK_ARCH): both pull the same
# per-arch tarball, so the hashes live in one place. Update these together
# with SWIFT_VERSION in swift-version.inc whenever the pinned release changes.
#
# swift.org only publishes x86_64 and aarch64 host bundles.
def swift_bundle_checksum(d, arch):
    sha256 = {
        "x86_64":  "3b4f249fc45a30e61a59684a4098714c2790802efa8e0b6539df66a9ae8ce5a2",
        "aarch64": "8898b043496f4634fa5366a37f8374acea4c19a5c49d306a405670bf21d31c68",
    }
    if arch not in sha256:
        bb.fatal('swift.org bundle: no checksum for arch {}'.format(arch))
    return sha256[arch]

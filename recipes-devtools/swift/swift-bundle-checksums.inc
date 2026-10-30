# SHA256 checksums for the swift.org prebuilt host toolchain bundles
# (download.swift.org, ubi9). Shared by swift-native.bb (keyed on
# HOST_ARCH) and nativesdk-swift.bb (keyed on SDK_ARCH): both pull the same
# per-arch tarball, so the hashes live in one place. Update these together
# with SWIFT_VERSION in swift-version.inc whenever the pinned release changes.
#
# swift.org only publishes x86_64 and aarch64 host bundles.
def swift_bundle_checksum(d, arch):
    sha256 = {
        "x86_64":  "eddef6430467da665abc4d91f947e0bf5fe00fb2fafe465910a8879be81613be",
        "aarch64": "d5a20569173239b501dade2f353ec7625d58fb97f2ed4e32a4579cf429571ef9",
    }
    if arch not in sha256:
        bb.fatal('swift.org bundle: no checksum for arch {}'.format(arch))
    return sha256[arch]

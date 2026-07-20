# SHA256 checksums for the swift.org prebuilt host toolchain bundles
# (download.swift.org, amazonlinux2). Shared by swift-native.bb (keyed on
# HOST_ARCH) and nativesdk-swift.bb (keyed on SDK_ARCH): both pull the same
# per-arch tarball, so the hashes live in one place. Update these together
# with SWIFT_VERSION in swift-version.inc whenever the pinned release changes.
#
# swift.org only publishes x86_64 and aarch64 host bundles.
def swift_bundle_checksum(d, arch):
    sha256 = {
        "x86_64":  "9991d195cdf81f232e789fc6cda3f80fbc0ae053679c1b51b49da7292a526f8a",
        "aarch64": "06ee7f708c6487c2bfe7fcf48ac859693701deb12a3e52c4706d3501e6bbfc78",
    }
    if arch not in sha256:
        bb.fatal('swift.org bundle: no checksum for arch {}'.format(arch))
    return sha256[arch]

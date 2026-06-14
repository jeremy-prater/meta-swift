# SHA256 checksums for the swift.org prebuilt host toolchain bundles
# (download.swift.org, amazonlinux2). Shared by swift-native.bb (keyed on
# HOST_ARCH) and nativesdk-swift.bb (keyed on SDK_ARCH): both pull the same
# per-arch tarball, so the hashes live in one place. Update these together
# with SWIFT_VERSION in swift-version.inc whenever the pinned release changes.
#
# swift.org only publishes x86_64 and aarch64 host bundles.
def swift_bundle_checksum(d, arch):
    sha256 = {
        "x86_64":  "a900331cf0f07f3bfc74f17d6b9d6a84fd02f8e2d66387061158f4b44c21f148",
        "aarch64": "bc2546727d8f01cae2cf0d419f2ea4209898e55f419bd630ed38c4031ca0801c",
    }
    if arch not in sha256:
        bb.fatal('swift.org bundle: no checksum for arch {}'.format(arch))
    return sha256[arch]

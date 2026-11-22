# SHA256 checksums for the swift.org prebuilt host toolchain bundles
# (download.swift.org, ubi9). Shared by swift-native.bb (keyed on
# HOST_ARCH) and nativesdk-swift.bb (keyed on SDK_ARCH): both pull the same
# per-arch tarball, so the hashes live in one place. Update these together
# with SWIFT_VERSION in swift-version.inc whenever the pinned release changes.
#
# swift.org only publishes x86_64 and aarch64 host bundles.
def swift_bundle_checksum(d, arch):
    sha256 = {
        "x86_64":  "26ff8b0835c260ad161bdb9f8cd4aab3e50682a2d67666fac6a24003737901d1",
        "aarch64": "49b07ec01f262f6f981cb8efd24dfa2fdb972746cdfa4edb8ebd9d4e4ec4b780",
    }
    if arch not in sha256:
        bb.fatal('swift.org bundle: no checksum for arch {}'.format(arch))
    return sha256[arch]

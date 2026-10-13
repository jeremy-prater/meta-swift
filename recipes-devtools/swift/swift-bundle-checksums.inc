# SHA256 checksums for the swift.org prebuilt host toolchain bundles
# (download.swift.org, ubuntu22.04). Shared by swift-native.bb (keyed on
# HOST_ARCH) and nativesdk-swift.bb (keyed on SDK_ARCH): both pull the same
# per-arch tarball, so the hashes live in one place. Update these together
# with SWIFT_VERSION in swift-version.inc whenever the pinned release changes.
#
# swift.org only publishes x86_64 and aarch64 host bundles.
def swift_bundle_checksum(d, arch):
    sha256 = {
        "x86_64":  "e9ec2ff05ac0fc89bf2e7cecdee5da4d398898a768d7ee2b86a59d2a7f01c44b",
        "aarch64": "5d50538c0500ff1b253e799f3e48f04db0547856aef76905c85e5bdfe01e6901",
    }
    if arch not in sha256:
        bb.fatal('swift.org bundle: no checksum for arch {}'.format(arch))
    return sha256[arch]

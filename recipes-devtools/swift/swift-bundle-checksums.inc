# SHA256 checksums for the swift.org prebuilt host toolchain bundles
# (download.swift.org, ubuntu22.04). Shared by swift-native.bb (keyed on
# HOST_ARCH) and nativesdk-swift.bb (keyed on SDK_ARCH): both pull the same
# per-arch tarball, so the hashes live in one place. Update these together
# with SWIFT_VERSION in swift-version.inc whenever the pinned release changes.
#
# swift.org only publishes x86_64 and aarch64 host bundles.
def swift_bundle_checksum(d, arch):
    sha256 = {
        "x86_64":  "bce85ea80f1471b85f39993f9285dbc798ec8e1eac5f677e24b2ac693856f9f8",
        "aarch64": "28c28acb2b91ed0e3beff5e6ba3695a785e645d9bd374adcaa3b7c2a901bc763",
    }
    if arch not in sha256:
        bb.fatal('swift.org bundle: no checksum for arch {}'.format(arch))
    return sha256[arch]

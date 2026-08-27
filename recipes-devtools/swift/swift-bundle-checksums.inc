# SHA256 checksums for the swift.org prebuilt host toolchain bundles
# (download.swift.org, ubuntu22.04 -- amazonlinux2 bundles stopped being
# published for 6.4 snapshots, which also raised the bundle's minimum host
# glibc from ~2.26 to ~2.34: RHEL 8 / Debian 11 class hosts can no longer run
# the prebuilt toolchain). Shared by swift-native-tarball.inc (keyed on
# HOST_ARCH) and nativesdk-swift.bb (keyed on SDK_ARCH): both pull the same
# per-arch tarball, so the hashes live in one place. Update these together
# with SWIFT_VERSION in swift-version.inc whenever the pinned release changes.
#
# swift.org only publishes x86_64 and aarch64 host bundles.
def swift_bundle_checksum(d, arch):
    sha256 = {
        "x86_64":  "b938816eb13afcf3fc64b0ba148a4588ce908be60041586f124b7fdc45ed32b9",
        "aarch64": "383a4256fb61f3919c1e4f1ceaae808d83fc416e9565b1dbb42b772fd4d60d4f",
    }
    if arch not in sha256:
        bb.fatal('swift.org bundle: no checksum for arch {}'.format(arch))
    return sha256[arch]

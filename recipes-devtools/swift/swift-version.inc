# Swift 6.4 development snapshot.
#
# Swift 6.4 has not been released yet (upstream main is already 6.5-dev), so 6.4
# is only published as development snapshots. Their git tags carry a "6.4.x"
# infix (swift-6.4.x-DEVELOPMENT-SNAPSHOT-<date>) and the prebuilt toolchains
# live under the "swift-6.4.x-branch" directory on download.swift.org. Roll to a
# newer snapshot by bumping SWIFT_SNAPSHOT (and the checksums in swift-native.bb).
#
# When 6.4 is released, switch off the snapshot scheme:
#   - SWIFT_TAG          = "swift-${SWIFT_VERSION}-RELEASE"  (e.g. swift-6.4-RELEASE)
#   - SWIFT_DOWNLOAD_DIR = "swift-${SWIFT_VERSION}-release"
#   - drop SWIFT_SNAPSHOT, and update the release-tarball sha256s in swift-native.bb
#   - re-confirm the two patches we dropped as already-upstream are still present
#     in the release: swift-stdlib 0003 (MetadataAccessor ABI) and
#     swift-foundation-essentials 0001 (64-bit fsblkcnt_t).
SWIFT_VERSION = "6.4"
SWIFT_SNAPSHOT = "2026-06-11-a"
SWIFT_TAG = "swift-6.4.x-DEVELOPMENT-SNAPSHOT-${SWIFT_SNAPSHOT}"

# download.swift.org sub-directory for the prebuilt native toolchain. Releases
# use "swift-${SWIFT_VERSION}-release"; snapshots use the branch directory.
SWIFT_DOWNLOAD_DIR = "swift-6.4.x-branch"

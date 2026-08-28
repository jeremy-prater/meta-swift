# Swift 6.4 development snapshot.
#
# Swift 6.4 has not been released yet (upstream main is already 6.5-dev), so 6.4
# is only published as development snapshots. Their git tags carry a "6.4.x"
# infix (swift-6.4.x-DEVELOPMENT-SNAPSHOT-<date>) and the prebuilt toolchains
# live under the "swift-6.4.x-branch" directory on download.swift.org. Roll to a
# newer snapshot by bumping SWIFT_SNAPSHOT (and the bundle checksums in
# swift-bundle-checksums.inc).
#
# When 6.4 is released, switch off the snapshot scheme:
#   - SWIFT_TAG          = "swift-${SWIFT_VERSION}-RELEASE"  (e.g. swift-6.4-RELEASE)
#   - SWIFT_DOWNLOAD_DIR = "swift-${SWIFT_VERSION}-release"
#   - drop SWIFT_SNAPSHOT, and update the release-tarball sha256s in
#     swift-bundle-checksums.inc
#   - re-confirm the two patches we dropped as already-upstream are still present
#     in the release: swift-stdlib 0003 (MetadataAccessor ABI) and
#     swift-foundation-essentials 0001 (64-bit fsblkcnt_t).
SWIFT_VERSION = "6.4"
SWIFT_SNAPSHOT = "2026-08-14-a"
# The "6.4.x" series infix in snapshot tags and download.swift.org paths,
# derived so a SWIFT_VERSION bump cannot silently keep fetching the old series.
SWIFT_SERIES = "${SWIFT_VERSION}.x"
SWIFT_TAG = "swift-${SWIFT_SERIES}-DEVELOPMENT-SNAPSHOT-${SWIFT_SNAPSHOT}"

# download.swift.org sub-directory for the prebuilt native toolchain. Releases
# use "swift-${SWIFT_VERSION}-release"; snapshots use the branch directory.
SWIFT_DOWNLOAD_DIR = "swift-${SWIFT_SERIES}-branch"

# Dependencies that Swift's utils/update_checkout pins per branch scheme rather
# than by release tag. Sourced from the release/6.4.x scheme in the snapshot's
# own update-checkout-config.json; re-diff on every SWIFT_SNAPSHOT bump, these
# do move between snapshots (subprocess 0.2.1 -> 0.4 and toolchain-sqlite
# 1.0.7 -> 1.0.9 both changed between 6.3.3 and the 6.4 snapshots).
SWIFT_ARGUMENT_PARSER_VERSION = "1.6.1"
SWIFT_COLLECTIONS_VERSION = "1.1.6"
SWIFT_SYSTEM_VERSION = "1.5.0"
SWIFT_TOOLCHAIN_SQLITE_VERSION = "1.0.9"
SWIFT_ASN1_VERSION = "1.3.2"
SWIFT_CERTIFICATES_VERSION = "1.10.1"
SWIFT_CRYPTO_VERSION = "3.12.5"
SWIFT_ATOMICS_VERSION = "1.2.0"
SWIFT_LOG_VERSION = "1.5.4"
SWIFT_NUMERICS_VERSION = "1.0.2"
SWIFT_SUBPROCESS_VERSION = "0.4"

# swift-tools-protocols is the odd one out: as of 6.4 update-checkout follows a
# BRANCH (release/6.4.x) for it rather than a semver tag, and unlike the other
# swiftlang repos it carries no swift-6.4.x-DEVELOPMENT-SNAPSHOT-* tag -- only
# main-line swift-DEVELOPMENT-SNAPSHOT-* ones. So it gets a branch plus an
# explicit SRCREV instead of a tag. Re-pin to the release/6.4.x tip when
# bumping SWIFT_SNAPSHOT; the snapshot tags don't record which commit upstream
# actually resolved the branch to.
SWIFT_TOOLS_PROTOCOLS_BRANCH = "release/${SWIFT_SERIES}"
SWIFT_TOOLS_PROTOCOLS_SRCREV = "4345e69c5572ec3d1f737fba90bca024b32da5cf"

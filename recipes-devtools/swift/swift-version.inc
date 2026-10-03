SWIFT_VERSION = "6.4.0"
SWIFT_TAG = "swift-${SWIFT_VERSION}-RELEASE"

# Dependencies that Swift's utils/update_checkout pins per branch scheme rather
# than by release tag. Sourced from the release/6.4.0 scheme in
# swift-6.4.0-RELEASE's update-checkout-config.json; re-diff on every
# SWIFT_VERSION bump, these do move between releases (subprocess 0.2.1 -> 0.4
# and toolchain-sqlite 1.0.7 -> 1.0.9 both changed between 6.3.3 and 6.4).
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

# From 6.4 update-checkout follows a branch (release/6.4.0) for
# swift-tools-protocols rather than a semver tag, but the repo is tagged
# swift-6.4.0-RELEASE on that branch's commit, so it takes the release tag.
SWIFT_TOOLS_PROTOCOLS_VERSION = "${SWIFT_TAG}"

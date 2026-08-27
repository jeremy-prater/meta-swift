# The bootstrap toolchain's source checkout: the same repo set as
# swift-source.inc, pinned at the bootstrap's own frozen 6.3.x revisions (see
# swift-bootstrap-version.inc for why the bootstrap cannot follow
# SWIFT_VERSION). Everything here is just the mapping from the shared list's
# SWIFT_SRC_* knobs onto the SWIFT_BOOTSTRAP_* pins; the list itself lives in
# swift-source.inc.

SWIFT_SRC_TAG = "${SWIFT_BOOTSTRAP_TAG}"
SWIFT_SRC_ARGUMENT_PARSER_VERSION = "${SWIFT_BOOTSTRAP_ARGUMENT_PARSER_VERSION}"
SWIFT_SRC_COLLECTIONS_VERSION = "${SWIFT_BOOTSTRAP_COLLECTIONS_VERSION}"
SWIFT_SRC_SYSTEM_VERSION = "${SWIFT_BOOTSTRAP_SYSTEM_VERSION}"
SWIFT_SRC_TOOLCHAIN_SQLITE_VERSION = "${SWIFT_BOOTSTRAP_TOOLCHAIN_SQLITE_VERSION}"
SWIFT_SRC_ASN1_VERSION = "${SWIFT_BOOTSTRAP_ASN1_VERSION}"
SWIFT_SRC_CERTIFICATES_VERSION = "${SWIFT_BOOTSTRAP_CERTIFICATES_VERSION}"
SWIFT_SRC_CRYPTO_VERSION = "${SWIFT_BOOTSTRAP_CRYPTO_VERSION}"
SWIFT_SRC_ATOMICS_VERSION = "${SWIFT_BOOTSTRAP_ATOMICS_VERSION}"
SWIFT_SRC_LOG_VERSION = "${SWIFT_BOOTSTRAP_LOG_VERSION}"
SWIFT_SRC_NUMERICS_VERSION = "${SWIFT_BOOTSTRAP_NUMERICS_VERSION}"
SWIFT_SRC_SUBPROCESS_VERSION = "${SWIFT_BOOTSTRAP_SUBPROCESS_VERSION}"

# At 6.3.x tools-protocols is a plain semver tag: no branch, no SRCREV.
SWIFT_SRC_TOOLSPROTOCOLS_URI = "git://github.com/swiftlang/swift-tools-protocols.git;protocol=https;name=toolsprotocols;tag=${SWIFT_BOOTSTRAP_TOOLS_PROTOCOLS_VERSION};nobranch=1;destsuffix=swift-tools-protocols;"
SWIFT_SRC_TOOLSPROTOCOLS_SRCREV = ""

require swift-source.inc

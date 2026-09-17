# THE BOOTSTRAP TOOLCHAIN'S OWN VERSION -- deliberately decoupled from
# swift-version.inc (the version actually being shipped).
#
# Why they must differ: swiftlang/swift commit e3641f68cc2 (2026-01-22) makes
# a host Swift compiler MANDATORY for building the compiler, so from 6.4 on
# there is no Swift-free bootstrap. 6.3.x is the last series where stage 0
# builds from a C/C++ compiler alone. The bootstrap therefore stays pinned at
# 6.3.3 no matter what SWIFT_VERSION moves to, and 6.4+ is reached by using
# the 6.3.3 toolchain as its host compiler.
#
# 6.4 imposes no "previous release" rule on the host swiftc -- its only
# version gate is CMAKE_Swift_COMPILER_VERSION VERSION_LESS 5.9 in
# AddPureSwift.cmake, and that is a C++-interop flag fallback, not a
# rejection. So this pin can stay put as SWIFT_VERSION advances.
#
# DO NOT bump this to match SWIFT_VERSION. Raising it past 6.3.x reintroduces
# the #error and breaks the bootstrap. If it ever must move, verify first:
#   curl -sfL https://raw.githubusercontent.com/swiftlang/swift/<tag>/lib/SILOptimizer/PassManager/PassManager.cpp | grep "not supported anymore"
# An empty result means that version can still self-bootstrap.

SWIFT_BOOTSTRAP_VERSION = "6.3.3"
SWIFT_BOOTSTRAP_TAG = "swift-${SWIFT_BOOTSTRAP_VERSION}-RELEASE"

# Semver-tagged dependencies that Swift's utils/update_checkout pins per
# release. Sourced from swiftlang/swift's release/6.3 branch
# update-checkout-config.json. Bump alongside SWIFT_BOOTSTRAP_VERSION.
SWIFT_BOOTSTRAP_ARGUMENT_PARSER_VERSION = "1.6.1"
SWIFT_BOOTSTRAP_COLLECTIONS_VERSION = "1.1.6"
SWIFT_BOOTSTRAP_SYSTEM_VERSION = "1.5.0"
SWIFT_BOOTSTRAP_TOOLCHAIN_SQLITE_VERSION = "1.0.7"
SWIFT_BOOTSTRAP_TOOLS_PROTOCOLS_VERSION = "0.0.9"
SWIFT_BOOTSTRAP_ASN1_VERSION = "1.3.2"
SWIFT_BOOTSTRAP_CERTIFICATES_VERSION = "1.10.1"
SWIFT_BOOTSTRAP_CRYPTO_VERSION = "3.12.5"
SWIFT_BOOTSTRAP_ATOMICS_VERSION = "1.2.0"
SWIFT_BOOTSTRAP_LOG_VERSION = "1.5.4"
SWIFT_BOOTSTRAP_NUMERICS_VERSION = "1.0.2"
SWIFT_BOOTSTRAP_SUBPROCESS_VERSION = "0.2.1"

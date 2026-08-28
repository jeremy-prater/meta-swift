# Upstream Swift source tree, enumerated as bitbake SRC_URI entries.
#
# Deliberately NOT utils/update-checkout (see PR #59's do_swift_checkout):
# keeping the repos in the fetcher is what buys offline builds
# (BB_NO_NETWORK), PREMIRRORS/DL_DIR reuse, revisions visible to `bitbake -S`,
# and SPDX/license tracking across all thirty-odd sources. If update-checkout's
# --dump-hashes ergonomics are wanted, generate the revisions INTO this file
# rather than cloning from inside a task.
#
# destsuffix values match update-checkout-config.json's on-disk names so
# utils/build-script finds each repo as a sibling of swift/.
#
# swift-docc, swift-docc-symbolkit, swift-docc-render-artifact (~1GB of web
# assets), swift-markdown and swift-format are deliberately NOT fetched: the
# presets build neither swift-docc nor swift-format (their local-deps
# workspace drags in swift-nio / swift-lmdb), and nothing in swiftpm's
# bootstrap references them. Fetching them cost multi-GB per cold build and
# put five never-compiled repos into the do_fetch/do_unpack signatures.
#
# Consumed twice, at different versions: swift-native fetches SWIFT_VERSION
# (the toolchain being shipped, defaults below), while swift-stage0-native
# overrides the SWIFT_SRC_* knobs to the frozen 6.3.x bootstrap pins via
# swift-bootstrap-source.inc. The knobs exist because the two checkouts
# genuinely differ in more than the tag: several semver pins moved between
# 6.3.3 and 6.4, and swift-tools-protocols changed SHAPE (a semver tag through
# 6.3.x, a branch plus explicit SRCREV from 6.4 on) -- hence the whole
# tools-protocols entry, and its SRCREV, being overridable rather than just a
# version string.

SWIFT_SRC_TAG ??= "${SWIFT_TAG}"
SWIFT_SRC_ARGUMENT_PARSER_VERSION ??= "${SWIFT_ARGUMENT_PARSER_VERSION}"
SWIFT_SRC_COLLECTIONS_VERSION ??= "${SWIFT_COLLECTIONS_VERSION}"
SWIFT_SRC_SYSTEM_VERSION ??= "${SWIFT_SYSTEM_VERSION}"
SWIFT_SRC_TOOLCHAIN_SQLITE_VERSION ??= "${SWIFT_TOOLCHAIN_SQLITE_VERSION}"
SWIFT_SRC_ASN1_VERSION ??= "${SWIFT_ASN1_VERSION}"
SWIFT_SRC_CERTIFICATES_VERSION ??= "${SWIFT_CERTIFICATES_VERSION}"
SWIFT_SRC_CRYPTO_VERSION ??= "${SWIFT_CRYPTO_VERSION}"
SWIFT_SRC_ATOMICS_VERSION ??= "${SWIFT_ATOMICS_VERSION}"
SWIFT_SRC_LOG_VERSION ??= "${SWIFT_LOG_VERSION}"
SWIFT_SRC_NUMERICS_VERSION ??= "${SWIFT_NUMERICS_VERSION}"
SWIFT_SRC_SUBPROCESS_VERSION ??= "${SWIFT_SUBPROCESS_VERSION}"
SWIFT_SRC_TOOLSPROTOCOLS_URI ??= "git://github.com/swiftlang/swift-tools-protocols.git;protocol=https;name=toolsprotocols;branch=${SWIFT_TOOLS_PROTOCOLS_BRANCH};destsuffix=swift-tools-protocols;"
SWIFT_SRC_TOOLSPROTOCOLS_SRCREV ??= "${SWIFT_TOOLS_PROTOCOLS_SRCREV}"

# A branch-form tools-protocols entry needs its SRCREV; a tag-form one must
# NOT have one set (a stray SRCREV overrides the tag and fetches the wrong
# revision). Set it only when the consumer supplies one.
python () {
    rev = d.getVar('SWIFT_SRC_TOOLSPROTOCOLS_SRCREV')
    if rev:
        d.setVar('SRCREV_toolsprotocols', rev)
}

SRCREV_FORMAT = "llvm_swift_cmark_libdispatch_corelibsxctest_swifttesting_foundation_corelibsfoundation_foundationicu_stringproc_syntax_driver_tsc_swiftpm_llbuild_argumentparser_collections_system_toolchainsqlite_toolsprotocols_asn1_certificates_crypto_atomics_log_numerics_subprocess_build"

SWIFT_SRC_URI = "\
    git://github.com/swiftlang/llvm-project.git;protocol=https;name=llvm;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=llvm-project; \
    git://github.com/swiftlang/swift.git;protocol=https;name=swift;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=swift; \
    git://github.com/swiftlang/swift-cmark.git;protocol=https;name=cmark;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=cmark; \
    git://github.com/swiftlang/swift-corelibs-libdispatch.git;protocol=https;name=libdispatch;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=swift-corelibs-libdispatch; \
    git://github.com/swiftlang/swift-corelibs-xctest.git;protocol=https;name=corelibsxctest;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=swift-corelibs-xctest; \
    git://github.com/swiftlang/swift-testing.git;protocol=https;name=swifttesting;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=swift-testing; \
    git://github.com/swiftlang/swift-foundation.git;protocol=https;name=foundation;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=swift-foundation; \
    git://github.com/swiftlang/swift-corelibs-foundation.git;protocol=https;name=corelibsfoundation;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=swift-corelibs-foundation; \
    git://github.com/swiftlang/swift-foundation-icu.git;protocol=https;name=foundationicu;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=swift-foundation-icu; \
    git://github.com/swiftlang/swift-experimental-string-processing.git;protocol=https;name=stringproc;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=swift-experimental-string-processing; \
    git://github.com/swiftlang/swift-syntax.git;protocol=https;name=syntax;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=swift-syntax; \
    git://github.com/swiftlang/swift-driver.git;protocol=https;name=driver;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=swift-driver; \
    git://github.com/swiftlang/swift-tools-support-core.git;protocol=https;name=tsc;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=swift-tools-support-core; \
    git://github.com/swiftlang/swift-package-manager.git;protocol=https;name=swiftpm;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=swiftpm; \
    git://github.com/swiftlang/swift-llbuild.git;protocol=https;name=llbuild;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=llbuild; \
    git://github.com/apple/swift-argument-parser.git;protocol=https;name=argumentparser;tag=${SWIFT_SRC_ARGUMENT_PARSER_VERSION};nobranch=1;destsuffix=swift-argument-parser; \
    git://github.com/apple/swift-collections.git;protocol=https;name=collections;tag=${SWIFT_SRC_COLLECTIONS_VERSION};nobranch=1;destsuffix=swift-collections; \
    git://github.com/apple/swift-system.git;protocol=https;name=system;tag=${SWIFT_SRC_SYSTEM_VERSION};nobranch=1;destsuffix=swift-system; \
    git://github.com/swiftlang/swift-toolchain-sqlite.git;protocol=https;name=toolchainsqlite;tag=${SWIFT_SRC_TOOLCHAIN_SQLITE_VERSION};nobranch=1;destsuffix=swift-toolchain-sqlite; \
    ${SWIFT_SRC_TOOLSPROTOCOLS_URI} \
    git://github.com/apple/swift-asn1.git;protocol=https;name=asn1;tag=${SWIFT_SRC_ASN1_VERSION};nobranch=1;destsuffix=swift-asn1; \
    git://github.com/apple/swift-certificates.git;protocol=https;name=certificates;tag=${SWIFT_SRC_CERTIFICATES_VERSION};nobranch=1;destsuffix=swift-certificates; \
    git://github.com/apple/swift-crypto.git;protocol=https;name=crypto;tag=${SWIFT_SRC_CRYPTO_VERSION};nobranch=1;destsuffix=swift-crypto; \
    git://github.com/apple/swift-atomics.git;protocol=https;name=atomics;tag=${SWIFT_SRC_ATOMICS_VERSION};nobranch=1;destsuffix=swift-atomics; \
    git://github.com/apple/swift-log.git;protocol=https;name=log;tag=${SWIFT_SRC_LOG_VERSION};nobranch=1;destsuffix=swift-log; \
    git://github.com/apple/swift-numerics.git;protocol=https;name=numerics;tag=${SWIFT_SRC_NUMERICS_VERSION};nobranch=1;destsuffix=swift-numerics; \
    git://github.com/swiftlang/swift-subprocess.git;protocol=https;name=subprocess;tag=${SWIFT_SRC_SUBPROCESS_VERSION};nobranch=1;destsuffix=swift-subprocess; \
    git://github.com/swiftlang/swift-build.git;protocol=https;name=build;tag=${SWIFT_SRC_TAG};nobranch=1;destsuffix=swift-build; \
"

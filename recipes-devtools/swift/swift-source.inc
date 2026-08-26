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
# Shared by swift-native (the stage-2 toolchain) and swift-stage0-native (the
# Swift-free bootstrap compiler). Both fetch the full set: DL_DIR is shared, so
# the cost is duplicated unpack, not duplicated download. Pruning stage 0 down
# to the repos it actually consumes (llvm-project, swift, cmark, libdispatch,
# experimental-string-processing) is a worthwhile follow-up once the pure-source
# path is known-good -- guessing the minimal set before it builds once is how
# you get a five-hour failure.

SRCREV_FORMAT = "llvm_swift_cmark_libdispatch_corelibsxctest_swifttesting_foundation_corelibsfoundation_foundationicu_stringproc_syntax_driver_tsc_swiftpm_llbuild_argumentparser_collections_system_toolchainsqlite_toolsprotocols_docc_doccsymbolkit_doccrenderartifact_markdown_format_asn1_certificates_crypto_atomics_log_numerics_subprocess_build"

SWIFT_SRC_URI = "\
    git://github.com/swiftlang/llvm-project.git;protocol=https;name=llvm;tag=${SWIFT_TAG};nobranch=1;destsuffix=llvm-project; \
    git://github.com/swiftlang/swift.git;protocol=https;name=swift;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift; \
    git://github.com/swiftlang/swift-cmark.git;protocol=https;name=cmark;tag=${SWIFT_TAG};nobranch=1;destsuffix=cmark; \
    git://github.com/swiftlang/swift-corelibs-libdispatch.git;protocol=https;name=libdispatch;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-corelibs-libdispatch; \
    git://github.com/swiftlang/swift-corelibs-xctest.git;protocol=https;name=corelibsxctest;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-corelibs-xctest; \
    git://github.com/swiftlang/swift-testing.git;protocol=https;name=swifttesting;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-testing; \
    git://github.com/swiftlang/swift-foundation.git;protocol=https;name=foundation;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-foundation; \
    git://github.com/swiftlang/swift-corelibs-foundation.git;protocol=https;name=corelibsfoundation;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-corelibs-foundation; \
    git://github.com/swiftlang/swift-foundation-icu.git;protocol=https;name=foundationicu;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-foundation-icu; \
    git://github.com/swiftlang/swift-experimental-string-processing.git;protocol=https;name=stringproc;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-experimental-string-processing; \
    git://github.com/swiftlang/swift-syntax.git;protocol=https;name=syntax;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-syntax; \
    git://github.com/swiftlang/swift-driver.git;protocol=https;name=driver;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-driver; \
    git://github.com/swiftlang/swift-tools-support-core.git;protocol=https;name=tsc;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-tools-support-core; \
    git://github.com/swiftlang/swift-package-manager.git;protocol=https;name=swiftpm;tag=${SWIFT_TAG};nobranch=1;destsuffix=swiftpm; \
    git://github.com/swiftlang/swift-llbuild.git;protocol=https;name=llbuild;tag=${SWIFT_TAG};nobranch=1;destsuffix=llbuild; \
    git://github.com/apple/swift-argument-parser.git;protocol=https;name=argumentparser;tag=${SWIFT_ARGUMENT_PARSER_VERSION};nobranch=1;destsuffix=swift-argument-parser; \
    git://github.com/apple/swift-collections.git;protocol=https;name=collections;tag=${SWIFT_COLLECTIONS_VERSION};nobranch=1;destsuffix=swift-collections; \
    git://github.com/apple/swift-system.git;protocol=https;name=system;tag=${SWIFT_SYSTEM_VERSION};nobranch=1;destsuffix=swift-system; \
    git://github.com/swiftlang/swift-toolchain-sqlite.git;protocol=https;name=toolchainsqlite;tag=${SWIFT_TOOLCHAIN_SQLITE_VERSION};nobranch=1;destsuffix=swift-toolchain-sqlite; \
    git://github.com/swiftlang/swift-tools-protocols.git;protocol=https;name=toolsprotocols;tag=${SWIFT_TOOLS_PROTOCOLS_VERSION};nobranch=1;destsuffix=swift-tools-protocols; \
    git://github.com/swiftlang/swift-docc.git;protocol=https;name=docc;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-docc; \
    git://github.com/swiftlang/swift-docc-symbolkit.git;protocol=https;name=doccsymbolkit;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-docc-symbolkit; \
    git://github.com/swiftlang/swift-docc-render-artifact.git;protocol=https;name=doccrenderartifact;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-docc-render-artifact; \
    git://github.com/swiftlang/swift-markdown.git;protocol=https;name=markdown;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-markdown; \
    git://github.com/swiftlang/swift-format.git;protocol=https;name=format;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-format; \
    git://github.com/apple/swift-asn1.git;protocol=https;name=asn1;tag=${SWIFT_ASN1_VERSION};nobranch=1;destsuffix=swift-asn1; \
    git://github.com/apple/swift-certificates.git;protocol=https;name=certificates;tag=${SWIFT_CERTIFICATES_VERSION};nobranch=1;destsuffix=swift-certificates; \
    git://github.com/apple/swift-crypto.git;protocol=https;name=crypto;tag=${SWIFT_CRYPTO_VERSION};nobranch=1;destsuffix=swift-crypto; \
    git://github.com/apple/swift-atomics.git;protocol=https;name=atomics;tag=${SWIFT_ATOMICS_VERSION};nobranch=1;destsuffix=swift-atomics; \
    git://github.com/apple/swift-log.git;protocol=https;name=log;tag=${SWIFT_LOG_VERSION};nobranch=1;destsuffix=swift-log; \
    git://github.com/apple/swift-numerics.git;protocol=https;name=numerics;tag=${SWIFT_NUMERICS_VERSION};nobranch=1;destsuffix=swift-numerics; \
    git://github.com/swiftlang/swift-subprocess.git;protocol=https;name=subprocess;tag=${SWIFT_SUBPROCESS_VERSION};nobranch=1;destsuffix=swift-subprocess; \
    git://github.com/swiftlang/swift-build.git;protocol=https;name=build;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-build; \
"

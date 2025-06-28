SUMMARY = "FoundationEssentials"
DESCRIPTION = "Foundation provides a base layer of functionality useful in many applications, including fundamental types for numbers, data, collections, and dates, as well as functions for task management, file system access, and more."
HOMEPAGE = "https://github.com/swiftlang/swift-foundation"

LICENSE = "Apache-2.0" 
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=2380e856fbdbc7ccae6bd699d53ec121"

require swift-version.inc
PV = "${SWIFT_VERSION}+git${SRCPV}"
SRCREV_FORMAT = "foundation_icu_syntax_collections"

SRC_URI = "git://github.com/swiftlang/swift-foundation.git;protocol=https;name=foundation;tag=swift-${SWIFT_VERSION}-RELEASE;nobranch=1;"
SRC_URI += "git://github.com/swiftlang/swift-foundation-icu.git;protocol=https;name=icu;tag=swift-${SWIFT_VERSION}-RELEASE;nobranch=1;destsuffix=swift-foundation-icu;"
SRC_URI += "git://github.com/swiftlang/swift-syntax.git;protocol=https;name=syntax;tag=swift-${SWIFT_VERSION}-RELEASE;nobranch=1;destsuffix=swift-foundation-icu;"
SRC_URI += "git://github.com/apple/swift-collections.git;protocol=https;nobranch=1;name=collections;tag=1.1.4;destsuffix=swift-collections;"
SRC_URI += "file://0001-build-with-64-bit-fsblkcnt_t-on-32-bit-glibc-platfor.patch;striplevel=1;"
SRC_URI += "file://0002-build-with-64-bit-time_t-on-32-bit-platforms.patch;striplevel=1;"

S = "${WORKDIR}/git"

DEPENDS = "icu swift-stdlib swift-native swift-foundation-icu"
RDEPENDS:${PN} += "icu swift-stdlib swift-foundation-icu"

inherit swift-cmake-base

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux"

# Enable Swift parts
EXTRA_OECMAKE += "-DENABLE_SWIFT=YES"
EXTRA_OECMAKE += "-DBUILD_SHARED_LIBS=YES"
EXTRA_OECMAKE += "-D_SwiftFoundationICU_SourceDIR=${WORKDIR}/swift-foundation-icu"
EXTRA_OECMAKE += "-D_SwiftCollections_SourceDIR=${WORKDIR}/swift-collections"
EXTRA_OECMAKE += "-DSwiftFoundation_MODULE_TRIPLE=${SWIFT_TARGET_NAME}"
EXTRA_OECMAKE += "-DSwiftSyntax_DIR=${WORKDIR}/swift-syntax/cmake/modules"

# Ensure the right CPU is targeted
cmake_do_generate_toolchain_file:append() {
    sed -i 's/set([ ]*CMAKE_SYSTEM_PROCESSOR .*[ ]*)/set(CMAKE_SYSTEM_PROCESSOR ${TARGET_CPU_NAME})/' ${WORKDIR}/toolchain.cmake
}

do_install:append() {
    # don't double up on Unicode
    rm -rf ${D}${libdir}/swift/_foundation_unicode
    rm -rf ${D}${libdir}/swift/linux/lib_FoundationICU.so
}

FILES:${PN} = "\
    ${libdir}/swift/linux/libFoundationEssentials.so \
    ${libdir}/swift/linux/libFoundationInternationalization.so \
"

FILES:${PN}-dev = "\
    ${libdir}/swift/_FoundationCShims/* \
    ${libdir}/swift/linux/FoundationEssentials.swiftmodule/* \
    ${libdir}/swift/linux/FoundationInternationalization.swiftmodule/* \
    ${libdir}/swift/linux/_FoundationCollections.swiftmodule/* \
"

FILES:${PN}-staticdev = "\
    ${libdir}/lib_SwiftLibraryPluginProviderCShims.a \
    ${libdir}/swift_static/linux/libFoundationEssentials.a \
"

INSANE_SKIP:${PN} = "file-rdeps"

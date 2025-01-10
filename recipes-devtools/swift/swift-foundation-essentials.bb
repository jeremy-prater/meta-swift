SUMMARY = "FoundationEssentials"
DESCRIPTION = "Foundation provides a base layer of functionality useful in many applications, including fundamental types for numbers, data, collections, and dates, as well as functions for task management, file system access, and more."
HOMEPAGE = "https://github.com/swiftlang/swift-foundation"

LICENSE = "Apache-2.0" 
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=2380e856fbdbc7ccae6bd699d53ec121"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRCREV_FORMAT = "swift_foundation"

SRC_URI = "git://github.com/swiftlang/swift-foundation.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1;"
SRC_URI += "git://github.com/swiftlang/swift-foundation-icu.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1;destsuffix=swift-foundation-icu;"
SRC_URI += "git://github.com/apple/swift-collections.git;protocol=https;tag=1.1.2;nobranch=1;destsuffix=swift-collections;"

S = "${WORKDIR}/git"

DEPENDS = "icu swift-stdlib swift-native"
RDEPENDS:${PN} += "icu swift-stdlib"

inherit swift-cmake-base

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux"

# Enable Swift parts
EXTRA_OECMAKE += "-DENABLE_SWIFT=YES"
EXTRA_OECMAKE += "-DBUILD_SHARED_LIBS=YES"
EXTRA_OECMAKE += "-D_SwiftFoundationICU_SourceDIR=${WORKDIR}/swift-foundation-icu"
EXTRA_OECMAKE += "-D_SwiftCollections_SourceDIR=${WORKDIR}/swift-collections"
EXTRA_OECMAKE += "-DSwiftFoundation_MODULE_TRIPLE=${TARGET_ARCH}-unknown-linux-gnu"

lcl_maybe_fortify="-D_FORTIFY_SOURCE=0"

# Ensure the right CPU is targeted
cmake_do_generate_toolchain_file:append() {
    sed -i 's/set([ ]*CMAKE_SYSTEM_PROCESSOR .*[ ]*)/set(CMAKE_SYSTEM_PROCESSOR ${TARGET_CPU_NAME})/' ${WORKDIR}/toolchain.cmake
}

FILES:${PN} = "${libdir}/swift/*"
INSANE_SKIP:${PN} = "file-rdeps"

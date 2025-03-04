SUMMARY = "Foundation"
DESCRIPTION = "The Foundation framework defines a base layer of functionality that is required for almost all applications."
HOMEPAGE = "https://github.com/swiftlang/swift-corelibs-foundation"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=1cd73afe3fb82e8d5c899b9d926451d0"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRCREV_FORMAT = "swift_corelibs_foundation"

SRC_URI = "git://github.com/swiftlang/swift-corelibs-foundation.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1;"
SRC_URI += "file://0001-Move-__CFAllocatorRespectsHintZeroWhenAllocating-to-.patch;striplevel=1;"
SRC_URI += "file://0001-CFRunLoopTimerGetTolerance-CFRunLoopTimerSetToleranc.patch;striplevel=1;"
SRC_URI += "git://github.com/swiftlang/swift-foundation.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1;destsuffix=swift-foundation;"
SRC_URI += "git://github.com/swiftlang/swift-foundation-icu.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1;destsuffix=swift-foundation-icu;"

S = "${WORKDIR}/git"

DEPENDS = "swift-foundation-essentials swift-stdlib swift-native libdispatch ncurses libxml2 icu curl"
RDEPENDS:${PN} += "swift-foundation-essentials swift-stdlib libdispatch"

inherit swift-cmake-base

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}${libdir}/swift/linux"

OECMAKE_C_FLAGS += "-I${STAGING_DIR_TARGET}${libdir}/swift"

EXTRA_OECMAKE += "-DENABLE_SWIFT=YES"
EXTRA_OECMAKE += "-DCMAKE_VERBOSE_MAKEFILE=ON"
EXTRA_OECMAKE += "-DCF_DEPLOYMENT_SWIFT=ON"

EXTRA_OECMAKE += "-D_SwiftFoundation_SourceDIR=${S}/swift-foundation"
EXTRA_OECMAKE += "-D_SwiftFoundationICU_SourceDIR=${S}/swift-foundation-icu"
EXTRA_OECMAKE += "-DSwiftFoundation_MODULE_TRIPLE=${TARGET_ARCH}-unknown-linux-gnu"

EXTRA_OECMAKE += "-DCMAKE_FIND_ROOT_PATH:PATH=${CROSS_COMPILE_DEPS_PATH}"

EXTRA_OECMAKE += "-Ddispatch_DIR=${STAGING_DIR_TARGET}${libdir}/swift/dispatch/cmake"

EXTRA_OECMAKE += "-DENABLE_TESTING=0"
EXTRA_OECMAKE += "-DBUILD_SHARED_LIBS=YES"
EXTRA_OECMAKE += "-DDISPATCH_INCLUDE_PATH=${STAGING_DIR_TARGET}/${includedir}"

lcl_maybe_fortify="-D_FORTIFY_SOURCE=0"

# Ensure the right CPU is targeted
cmake_do_generate_toolchain_file:append() {
    sed -i 's/set([ ]*CMAKE_SYSTEM_PROCESSOR .*[ ]*)/set(CMAKE_SYSTEM_PROCESSOR ${TARGET_CPU_NAME})/' ${WORKDIR}/toolchain.cmake
}

do_install:append() {
    # don't double up on Unicode
    rm -rf ${D}${libdir}/swift/_foundation_unicode

    # No need to install the plutil onto the target, so remove it for now
    rm ${D}${bindir}/plutil

    # Since plutil was the only thing in the bindir, remove the bindir as well
    rmdir ${D}${bindir}
}

FILES:${PN} = "${libdir}/swift/*"
INSANE_SKIP:${PN} = "file-rdeps"

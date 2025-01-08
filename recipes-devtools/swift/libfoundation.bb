SUMMARY = "The Foundation framework defines a base layer of functionality that is required for almost all applications."
HOMEPAGE = "https://github.com/swiftlang/swift-corelibs-foundation"

LICENSE = "Apache-2.0" 
LIC_FILES_CHKSUM = "file://LICENSE;md5=1cd73afe3fb82e8d5c899b9d926451d0"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRCREV_FORMAT = "swift_foundation"

SRC_URI = "git://github.com/swiftlang/swift-foundation.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1"
SRC_URI += "git://github.com/swiftlang/swift-foundation-icu.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1"
SRC_URI += "git://github.com/apple/swift-collections.git;protocol=https;tag=1.1.4;nobranch=1"
SRC_URI += "git://github.com/swiftlang/swift-corelibs-foundation.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1"

# TODO: add this patch
## sed -i '/__CFAllocatorRespectsHintZeroWhenAllocating/d' ${STAGING_LIBDIR}/swift/CoreFoundation/*Only.h

S = "${WORKDIR}/git"

DEPENDS = "swift-stdlib libdispatch ncurses libxml2 icu curl"
RDEPENDS:${PN} += "swift-stdlib libdispatch"

inherit swift-cmake-base

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux"

# Enable Swift parts
EXTRA_OECMAKE += "-DENABLE_SWIFT=YES"
EXTRA_OECMAKE += "-DCMAKE_Swift_FLAGS=${SWIFT_FLAGS}"
EXTRA_OECMAKE += "-DCMAKE_VERBOSE_MAKEFILE=ON"
EXTRA_OECMAKE += "-DCF_DEPLOYMENT_SWIFT=ON"

EXTRA_OECMAKE += "-DFOUNDATION_PATH_TO_LIBDISPATCH_SOURCE=${S}/libdispatch"
EXTRA_OECMAKE += "-DFOUNDATION_PATH_TO_LIBDISPATCH_BUILD=${STAGING_DIR_TARGET}/usr/lib/swift/dispatch"
EXTRA_OECMAKE += "-D_SwiftFoundation_SourceDIR=${S}/swift-foundation"
EXTRA_OECMAKE += "-D_SwiftFoundationICU_SourceDIR=${S}/swift-foundation-icu"
EXTRA_OECMAKE += "-D_SwiftCollections_SourceDIR=${S}/swift-collections"

#EXTRA_OECMAKE += "-DCMAKE_FIND_ROOT_PATH:PATH=${CROSS_COMPILE_DEPS_PATH}"

EXTRA_OECMAKE += "-Ddispatch_DIR=${STAGING_DIR_TARGET}/usr/lib/swift/dispatch/cmake"

EXTRA_OECMAKE += "-DENABLE_TESTING=0"
EXTRA_OECMAKE += "-DBUILD_SHARED_LIBS=YES"

lcl_maybe_fortify="-D_FORTIFY_SOURCE=0"

# Ensure the right CPU is targeted
cmake_do_generate_toolchain_file:append() {
    sed -i 's/set([ ]*CMAKE_SYSTEM_PROCESSOR .*[ ]*)/set(CMAKE_SYSTEM_PROCESSOR ${TARGET_CPU_NAME})/' ${WORKDIR}/toolchain.cmake
}

TEMP_DISPATCH_DIR = "${WORKDIR}/temp/dispatch"

do_configure:append() {
    # Workaround Dispatch defined with cmake and module
    mkdir -p "${TEMP_DISPATCH_DIR}"
    cp -rf ${STAGING_DIR_TARGET}/usr/lib/swift/dispatch/module.modulemap ${TEMP_DISPATCH_DIR}/module.modulemap
    rm -rf ${STAGING_DIR_TARGET}/usr/lib/swift/dispatch/module.modulemap
}

do_install:append() {
    # No need to install the plutil onto the target, so remove it for now
    rm ${D}${bindir}/plutil

    # Since plutil was the only thing in the bindir, remove the bindir as well
    rmdir ${D}${bindir}

    # Restore Dispatch
    cp -rf ${TEMP_DISPATCH_DIR}/module.modulemap ${STAGING_DIR_TARGET}/usr/lib/swift/dispatch/module.modulemap
}

FILES:${PN} = "${libdir}/swift/*"
INSANE_SKIP:${PN} = "file-rdeps"

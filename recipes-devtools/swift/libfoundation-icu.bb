SUMMARY = "This version of the ICU4C project contains customized extensions for use by the Foundation package."
HOMEPAGE = "https://github.com/swiftlang/swift-corelibs-foundation-icu"

LICENSE = "Apache-2.0" 
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=2380e856fbdbc7ccae6bd699d53ec121"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRCREV_FORMAT = "swift_foundation"

SRC_URI = "git://github.com/swiftlang/swift-foundation-icu.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1"

S = "${WORKDIR}/git"

DEPENDS = "icu swift-stdlib"
RDEPENDS:${PN} += "swift-stdlib"

inherit swift-cmake-base

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux"

# Enable Swift parts
EXTRA_OECMAKE += "-DENABLE_SWIFT=YES"
EXTRA_OECMAKE += "-DCMAKE_VERBOSE_MAKEFILE=ON"
EXTRA_OECMAKE += "-DCF_DEPLOYMENT_SWIFT=ON"

EXTRA_OECMAKE += "-DCMAKE_FIND_ROOT_PATH:PATH=${CROSS_COMPILE_DEPS_PATH}"

EXTRA_OECMAKE += "-DENABLE_TESTING=0"
EXTRA_OECMAKE += "-DBUILD_SHARED_LIBS=YES"

lcl_maybe_fortify="-D_FORTIFY_SOURCE=0"

# Ensure the right CPU is targeted
cmake_do_generate_toolchain_file:append() {
    sed -i 's/set([ ]*CMAKE_SYSTEM_PROCESSOR .*[ ]*)/set(CMAKE_SYSTEM_PROCESSOR ${TARGET_CPU_NAME})/' ${WORKDIR}/toolchain.cmake
}

FILES:${PN} = "${libdir}/swift/*"
INSANE_SKIP:${PN} = "file-rdeps"

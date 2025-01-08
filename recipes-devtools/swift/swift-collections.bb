SUMMARY = "Collections"
DESCRIPTION = "Commonly used data structures for Swift"
HOMEPAGE = "https://github.com/apple/swift-collections"

LICENSE = "Apache-2.0" 
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

PV = "1.1.4"

SRC_URI = "git://github.com/apple/swift-collections.git;protocol=https;tag=${PV};nobranch=1"

S = "${WORKDIR}/git"

DEPENDS = "swift-stdlib"
RDEPENDS:${PN} += "swift-stdlib"

inherit swift-cmake-base

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux"

# Enable Swift parts
EXTRA_OECMAKE += "-DENABLE_SWIFT=YES"
EXTRA_OECMAKE += "-DBUILD_SHARED_LIBS=YES"

lcl_maybe_fortify="-D_FORTIFY_SOURCE=0"

# Ensure the right CPU is targeted
cmake_do_generate_toolchain_file:append() {
    sed -i 's/set([ ]*CMAKE_SYSTEM_PROCESSOR .*[ ]*)/set(CMAKE_SYSTEM_PROCESSOR ${TARGET_CPU_NAME})/' ${WORKDIR}/toolchain.cmake
}

FILES:${PN} = "${libdir}/swift/*"
INSANE_SKIP:${PN} = "file-rdeps"

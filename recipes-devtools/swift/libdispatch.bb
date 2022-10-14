
SUMMARY = "Libdispatch"
HOMEPAGE = "https://github.com/apple/swift-corelibs-libdispatch"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=1cd73afe3fb82e8d5c899b9d926451d0"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_URI = "git://github.com/apple/swift-corelibs-libdispatch.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1"
#SRC_URI[sha256sum] = "2611b4dc9530207e19dae07599355622f76c32694aca3ef909149a7ecf48dfc7"

DEPENDS = "swift-stdlib ncurses"

S = "${WORKDIR}/git"

inherit swift-cmake-base

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux"

# Enable Swift parts
EXTRA_OECMAKE += "-DENABLE_SWIFT=YES"
EXTRA_OECMAKE += '-DCMAKE_Swift_FLAGS="${SWIFT_FLAGS}"'

# Ensure the right CPU is targeted
cmake_do_generate_toolchain_file_append() {
    sed -i 's/set([ ]*CMAKE_SYSTEM_PROCESSOR .*[ ]*)/set(CMAKE_SYSTEM_PROCESSOR ${TARGET_CPU_NAME})/' ${WORKDIR}/toolchain.cmake
}

do_install_append() {
    # Copy cmake build modules
    mkdir -p ${D}${libdir}/swift/dispatch/cmake
    cp -rf ${WORKDIR}/build/cmake/modules/* ${D}${libdir}/swift/dispatch/cmake/
}

FILES_${PN} = "${libdir}/swift/*"
INSANE_SKIP_${PN} = "file-rdeps"
do_package_qa[noexec] = "1"

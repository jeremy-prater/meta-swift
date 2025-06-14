
SUMMARY = "Libdispatch"
HOMEPAGE = "https://github.com/apple/swift-corelibs-libdispatch"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=1cd73afe3fb82e8d5c899b9d926451d0"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_URI = "git://github.com/apple/swift-corelibs-libdispatch.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1"

DEPENDS = "swift-stdlib ncurses"

S = "${WORKDIR}/git"


inherit swift-cmake-base

#python () {
#    rrec = d.getVar('CXXFLAGS', d, 1)
#    if rrec:
#        rrec = rrec.replace("-fcanon-prefix-map", "")
#        d.setVar('CXXFLAGS', rrec)
#}


python () {
    for var in ['CFLAGS', 'CXXFLAGS', 'CXXLDFLAGS','CLDFLAGS', 'LDFLAGS', 'EXTRA_OECMAKE']:
        flags = d.getVar(var, expand=True)
        if flags:
            flags = flags.replace("-fcanon-prefix-map", "")
            d.setVar(var, flags)
}

#CXXFLAGS := "${@oe_utils.str_filter_out('-fcanon-prefix-map[^ ]*', '${CXXFLAGS}', d)}"
#CXXFLAGS := "${@oe_filter_out('-fcanon-prefix-map[^ ]*', '${CXXFLAGS}', d)}"
#LDFLAGS := "${@oe_filter_out('-fcanon-prefix-map[^ ]*', '${LDFLAGS}', d)}"
#TARGET_CFLAGS := "${@oe_filter_out('-fcanon-prefix-map[^ ]*', '${TARGET_CFLAGS}', d)}"
#TARGET_CXXFLAGS := "${@oe_filter_out('-fcanon-prefix-map[^ ]*', '${TARGET_CXXFLAGS}', d)}"
#TARGET_LDFLAGS := "${@oe_filter_out('-fcanon-prefix-map[^ ]*', '${TARGET_LDFLAGS}', d)}"

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux"

# Enable Swift parts
EXTRA_OECMAKE += "-DENABLE_SWIFT=YES"

# Ensure the right CPU is targeted
cmake_do_generate_toolchain_file:append() {
sed -i 's/set([ ]*CMAKE_SYSTEM_PROCESSOR .*[ ]*)/set(CMAKE_SYSTEM_PROCESSOR ${TARGET_CPU_NAME})/' ${WORKDIR}/toolchain.cmake
sed -i 's|-fcanon-prefix-map[^ ]*||g' ${WORKDIR}/toolchain.cmake
}

do_install:append() {
    # Copy cmake build modules
    mkdir -p ${D}${libdir}/swift/dispatch/cmake
    cp -rf ${WORKDIR}/build/cmake/modules/* ${D}${libdir}/swift/dispatch/cmake/
}

FILES:${PN} += "${libdir}/swift/**"
INSANE_SKIP_${PN} = "file-rdeps"

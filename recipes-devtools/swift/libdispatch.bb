SUMMARY = "libdispatch"
DESCRIPTION = "The libdispatch Project, (a.k.a. Grand Central Dispatch), for concurrency on multicore hardware"
HOMEPAGE = "https://github.com/swiftlang/swift-corelibs-libdispatch"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE;md5=1cd73afe3fb82e8d5c899b9d926451d0"

require swift-version.inc
PV = "${SWIFT_VERSION}+git${SRCPV}"

DEPENDS = "swift-stdlib"

SRC_URI = "git://github.com/swiftlang/swift-corelibs-libdispatch.git;protocol=https;tag=${SWIFT_TAG};nobranch=1"

S = "${UNPACKDIR}/git"
LIBDISPATCH_BUILDDIR = "${UNPACKDIR}/build"

inherit swift-cmake-base

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux"

# Enable Swift parts
EXTRA_OECMAKE += "-DENABLE_SWIFT=YES"

# Ensure the right CPU is targeted
cmake_do_generate_toolchain_file:append() {
    sed -i 's/set([ ]*CMAKE_SYSTEM_PROCESSOR .*[ ]*)/set(CMAKE_SYSTEM_PROCESSOR ${TARGET_CPU_NAME})/' ${WORKDIR}/toolchain.cmake
}

do_install:append() {
    # move the header files out of /usr/lib/swift and into /usr/include,
    # because we want Swift C shims to be able to pick them up without fancy
    # SwiftPM magic
    install -d ${D}${includedir}
    (cd ${D}${libdir}/swift; mv Block os dispatch ${D}${includedir})

    # don't install CMake modules as they have absolute paths in them
#    install -d ${D}${libdir}/cmake/dispatch
#    install -m 0644 ${LIBDISPATCH_BUILDDIR}/cmake/modules/dispatchConfig.cmake ${D}${libdir}/cmake/dispatch/
#    install -m 0644 ${LIBDISPATCH_BUILDDIR}/cmake/modules/dispatchExports.cmake ${D}${libdir}/cmake/dispatch/
}

FILES:${PN} = "\
    ${libdir}/swift/linux/libdispatch.so \
    ${libdir}/swift/linux/libswiftDispatch.so \
    ${libdir}/swift/linux/libBlocksRuntime.so \
"

FILES:${PN}-dev = "\
    ${libdir}/swift/linux/${SWIFT_TARGET_ARCH}/Dispatch.swiftdoc \
    ${libdir}/swift/linux/${SWIFT_TARGET_ARCH}/Dispatch.swiftmodule \
    ${includedir}/Block \
    ${includedir}/os \
    ${includedir}/dispatch \
"

FILES:${PN}-staticdev = "\
    ${libdir}/swift_static/linux/libdispatch.a \
    ${libdir}/swift_static/linux/libswiftDispatch.a \
    ${libdir}/swift_static/linux/libBlocksRuntime.a \
    ${libdir}/swift_static/linux/libDispatchStubs.a \
    ${libdir}/swift_static/linux/${SWIFT_TARGET_ARCH}/Dispatch.swiftdoc \
    ${libdir}/swift_static/linux/${SWIFT_TARGET_ARCH}/Dispatch.swiftmodule \
    ${libdir}/swift_static/linux/dispatch \
"

INSANE_SKIP:${PN} = "file-rdeps buildpaths"
INSANE_SKIP:${PN}-dbg = "buildpaths"

SUMMARY = "Swift"
DESCRIPTION = "The Swift programming language standard library"
HOMEPAGE = "https://github.com/swiftlang/swift"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRCREV_FORMAT = "swift_stdlib"

SRC_URI = "\
    git://github.com/swiftlang/swift.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1;destsuffix=swift \
    git://github.com/swiftlang/swift-corelibs-libdispatch.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1;destsuffix=libdispatch \ 
    git://github.com/swiftlang/swift-experimental-string-processing.git;protocol=https;tag=swift/release/${PV};nobranch=1;destsuffix=swift-experimental-string-processing \
    git://github.com/swiftlang/swift-syntax.git;protocol=https;tag=release/${PV};nobranch=1;destsuffix=swift-syntax \
    file://cmake-configure-swift-stdlib.sh \
    file://llvm-cmake-modules \
    file://PR75367-buildbot-cross-compile.diff \
    "

S = "${WORKDIR}/swift"

SWIFT_BUILDDIR = "${S}/build"
DEPENDS = "gcc-runtime python3-native icu ncurses swift-native libgcc gcc glibc libxml2 libxml2-native"

inherit swift-cmake-base

TARGET_LDFLAGS += "-w -fuse-ld=lld -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/current"

SWIFT_CMAKE_TOOLCHAIN_FILE = "${WORKDIR}/linux-${SWIFT_TARGET_ARCH}-toolchain.cmake"
SWIFT_CONFIGURE_CMAKE_SCRIPT="${WORKDIR}/cmake-configure-swift-stdlib.sh"
SWIFT_C_FLAGS = "-w -fuse-ld=lld -target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION} -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION} -I${STAGING_DIR_TARGET}/usr/include ${EXTRA_INCLUDE_FLAGS}"
SWIFT_C_LINK_FLAGS = "-target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} ${EXTRA_INCLUDE_FLAGS}"
SWIFT_CXX_FLAGS = "-w -fuse-ld=lld -target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION} -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION} -I${STAGING_DIR_TARGET}/usr/include -B${STAGING_DIR_TARGET}/usr/lib ${EXTRA_INCLUDE_FLAGS}"
SWIFT_CXX_LINK_FLAGS = "-target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} ${EXTRA_INCLUDE_FLAGS}"

do_fix_gcc_install_dir() {
    # symbolic links do not work, will not be found by Swift clang driver
    # this is necessary to make the libstdc++ location heuristic work, necessary for C++ interop
    (cd ${STAGING_DIR_TARGET}/usr/lib; rm -rf gcc; mkdir gcc; cp -rp aarch64-oe-linux gcc)
}

addtask fix_gcc_install_dir after do_populate_sysroot

do_configure() {
    export LDFLAGS=""
    export STAGING_DIR_TARGET=${STAGING_DIR_TARGET}
    export SDKROOT=${STAGING_DIR_TARGET}
    export SWIFT_SRCDIR=${S}
    export SWIFT_TARGET_SYS=${TARGET_SYS}
    export LIBDISPATCH_SRCDIR=${WORKDIR}/libdispatch
    export SWIFT_BUILDDIR="${SWIFT_BUILDDIR}"
    export SWIFT_CMAKE_TOOLCHAIN_FILE=${SWIFT_CMAKE_TOOLCHAIN_FILE}
    export SWIFT_NATIVE_PATH=${STAGING_DIR_NATIVE}/usr/bin
    export SWIFT_C_FLAGS="${SWIFT_C_FLAGS}"
    export SWIFT_C_LINK_FLAGS="${SWIFT_C_LINK_FLAGS}"
    export SWIFT_CXX_FLAGS="${SWIFT_CXX_FLAGS}"
    export SWIFT_CXX_LINK_FLAGS="${SWIFT_CXX_LINK_FLAGS}"
    export SWIFT_LLVM_DIR=${HOST_LLVM_PATH}
    export CC=${STAGING_DIR_NATIVE}/usr/bin/clang
    export CFLAGS="${SWIFT_C_FLAGS}"
    export CCLD="${SWIFT_C_LINK_FLAGS}"
    export CXX=${STAGING_DIR_NATIVE}/usr/bin/clang++
    export CXXFLAGS="${SWIFT_CXX_FLAGS}"
    export SWIFT_TARGET_ARCH=${SWIFT_TARGET_ARCH}
    export SWIFT_TARGET_NAME=${SWIFT_TARGET_NAME}
    export SWIFT_PATH_TO_STRING_PROCESSING_SOURCE="${WORKDIR}/swift-experimental-string-processing"
    export SWIFT_SYNTAX_SOURCE_DIR="${WORKDIR}/swift-syntax"
    export SWIFT_GCC_VERSION=${SWIFT_GCC_VERSION}

    mkdir -p ${HOST_LLVM_PATH}/cmake/llvm
    cp ${WORKDIR}/llvm-cmake-modules/* ${HOST_LLVM_PATH}/cmake/llvm

    rm -rf ${SWIFT_BUILDDIR}
    mkdir -p ${SWIFT_BUILDDIR}
    ${SWIFT_CONFIGURE_CMAKE_SCRIPT}
}

do_compile() {
    cd ${SWIFT_BUILDDIR} && ninja
}

do_install:prepend() {
    # remove Swift static libs (if any)
    rm -rf ${SWIFT_BUILDDIR}/lib/swift_static

    # remove Dispatch (it will be built by another package)
    rm -f ${SWIFT_BUILDDIR}/lib/swift/linux/libBlocksRuntime.so
    rm -f ${SWIFT_BUILDDIR}/lib/swift/linux/libdispatch.so 
    rm -f ${SWIFT_BUILDDIR}/lib/swift/linux/${SWIFT_TARGET_ARCH}/*.so

    # remove some dirs from /lib/swift (we don't include them in any packages) 
    rm -rf ${SWIFT_BUILDDIR}/lib/swift/clang
    rm -rf ${SWIFT_BUILDDIR}/lib/swift/FrameworkABIBaseline

    # remove /usr/share (we don't include it in any packages) 
    rm -rf ${SWIFT_BUILDDIR}/share

    # remove /usr/bin (we don't include it in any packages)
    rm -rf ${SWIFT_BUILDDIR}/bin
}

do_install() {
    install -d ${D}${libdir}
    cp -rfd ${SWIFT_BUILDDIR}/lib/swift ${D}${libdir}/
}

FILES:${PN} = "${libdir}/swift"
INSANE_SKIP:${PN} = "file-rdeps"

do_package_qa[noexec] = "1"

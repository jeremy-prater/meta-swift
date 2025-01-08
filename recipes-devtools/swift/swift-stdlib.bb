
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRCREV_FORMAT = "swift_stdlib"

SRC_URI = "\
    git://github.com/swiftlang/swift.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1 \
    git://github.com/swiftlang/swift-corelibs-libdispatch.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1;destsuffix=libdispatch \ 
    file://cmake-configure-swift-stdlib.sh \
    file://llvm-cmake-modules \
    "

S = "${WORKDIR}/git"

SWIFT_BUILDDIR = "${S}/build"
DEPENDS = "gcc-runtime python3-native icu ncurses"
DEPENDS += " swift-native libgcc gcc glibc libxml2"

inherit swift-cmake-base

SWIFT_GCC_VERSION = "13.3.0"

EXTRA_INCLUDE_FLAGS = "\
    -I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_GCC_VERSION}/${TARGET_SYS} \
    -I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_GCC_VERSION} \
    -I${STAGING_DIR_TARGET}"

TARGET_LDFLAGS += "-w -fuse-ld=lld -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/current"

HOST_SWIFT_SUPPORT_DIR = "${WORKDIR}/swift-stdlib-yocto"
SWIFT_CMAKE_TOOLCHAIN_FILE = "${HOST_SWIFT_SUPPORT_DIR}/linux-${SWIFT_TARGET_ARCH}-toolchain.cmake"
SWIFT_CONFIGURE_CMAKE_SCRIPT="${WORKDIR}/cmake-configure-swift-stdlib.sh"
SWIFT_C_FLAGS = "-w -fuse-ld=lld -target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION} -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION} -I${STAGING_DIR_TARGET}/usr/include ${EXTRA_INCLUDE_FLAGS}"
SWIFT_C_LINK_FLAGS = "-target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} ${EXTRA_INCLUDE_FLAGS}"
SWIFT_CXX_FLAGS = "-w -fuse-ld=lld -target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION} -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION} -I${STAGING_DIR_TARGET}/usr/include -B${STAGING_DIR_TARGET}/usr/lib ${EXTRA_INCLUDE_FLAGS}"
SWIFT_CXX_LINK_FLAGS = "-target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} ${EXTRA_INCLUDE_FLAGS}"

do_configure() {
    export LDFLAGS=""
    export STAGING_DIR=${STAGING_DIR_TARGET}
    export SWIFT_SRCDIR=${S}
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

    mkdir -p ${HOST_LLVM_PATH}/cmake/llvm
    cp ${WORKDIR}/llvm-cmake-modules/* ${HOST_LLVM_PATH}/cmake/llvm

    mkdir -p ${HOST_SWIFT_SUPPORT_DIR}
    rm -rf $SWIFT_BUILDDIR
    mkdir -p $SWIFT_BUILDDIR
    ${SWIFT_CONFIGURE_CMAKE_SCRIPT}
}

do_compile() {
    cd ${SWIFT_BUILDDIR} && ninja
}

do_install:prepend() {
    # remove Swift static libs
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

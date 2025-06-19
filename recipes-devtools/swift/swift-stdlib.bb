SUMMARY = "Swift standard library"
HOMEPAGE = "https://swift.org/"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_URI = "git://github.com/apple/swift.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1 \
        file://0001-Float16.patch \
        file://fix_modulemap.sh \
        file://cmake-configure-swift-stdlib.sh \
        "
SRC_URI += "git://github.com/apple/swift-corelibs-libdispatch.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1;destsuffix=libdispatch" 

S = "${WORKDIR}/git"
SWIFT_BUILDDIR = "${S}/build"
DEPENDS = "gcc-runtime python3-native icu ncurses"
DEPENDS += " swift-native swift-llvm-native libgcc gcc glibc libxml2"

inherit swift-cmake-base

HOST_SWIFT_SUPPORT_DIR = "${WORKDIR}/swift-stdlib-yocto"
SWIFT_CMAKE_TOOLCHAIN_FILE = "${HOST_SWIFT_SUPPORT_DIR}/linux-${SWIFT_TARGET_ARCH}-toolchain.cmake"
SWIFT_CONFIGURE_CMAKE_SCRIPT="${WORKDIR}/cmake-configure-swift-stdlib.sh"
EXTRA_INCLUDE_FLAGS = "\
    -I${STAGING_DIR_TARGET}/usr/include/c++/current/${TARGET_SYS} \
    -I${STAGING_DIR_TARGET}/usr/include/c++/current"
TARGET_LDFLAGS += "-w -fuse-ld=lld -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/current"
SWIFT_C_FLAGS = "-w -fuse-ld=lld -target ${SWIFT_TARGET_NAME} \
    --sysroot ${STAGING_DIR_TARGET} \
    -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/current \
    -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/current \
    ${EXTRA_INCLUDE_FLAGS}"
SWIFT_C_LINK_FLAGS = "-target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} ${EXTRA_INCLUDE_FLAGS}"
SWIFT_CXX_FLAGS = "${SWIFT_C_FLAGS}"
SWIFT_CXX_LINK_FLAGS = "${SWIFT_C_LINK_FLAGS}"

do_configure() {
    export LDFLAGS=""
    export STAGING_DIR=${STAGING_DIR_TARGET}
    export SWIFT_SRCDIR=${S}
    export LIBDISPATCH_SRCDIR=${WORKDIR}/libdispatch
    export SWIFT_BUILDDIR="${SWIFT_BUILDDIR}"
    export SWIFT_CMAKE_TOOLCHAIN_FILE=${SWIFT_CMAKE_TOOLCHAIN_FILE}
    export SWIFT_NATIVE_PATH=${STAGING_DIR_NATIVE}/opt/usr/bin
    export SWIFT_C_FLAGS="${SWIFT_C_FLAGS}"
    export SWIFT_C_LINK_FLAGS="${SWIFT_C_LINK_FLAGS}"
    export SWIFT_CXX_FLAGS="${SWIFT_CXX_FLAGS}"
    export SWIFT_CXX_LINK_FLAGS="${SWIFT_CXX_LINK_FLAGS}"
    export SWIFT_LLVM_DIR=${HOST_LLVM_PATH}
    export CC=${STAGING_DIR_NATIVE}/opt/usr/bin/clang
    export CFLAGS="${SWIFT_C_FLAGS}"
    export CCLD="${SWIFT_C_LINK_FLAGS}"
    export CXX=${STAGING_DIR_NATIVE}/opt/usr/bin/clang++
    export CXXFLAGS="${SWIFT_CXX_FLAGS}"
    export SWIFT_TARGET_ARCH=${SWIFT_TARGET_ARCH}
    export SWIFT_TARGET_NAME=${SWIFT_TARGET_NAME}

    mkdir -p ${HOST_SWIFT_SUPPORT_DIR}
    rm -rf $SWIFT_BUILDDIR
    mkdir -p $SWIFT_BUILDDIR
    ${SWIFT_CONFIGURE_CMAKE_SCRIPT}
}

do_compile() {
    cd ${SWIFT_BUILDDIR} && ninja
    # remove Swift static libs
    rm -rf ${SWIFT_BUILDDIR}/lib/swift_static
    # remove Dispatch (it will be built by another package)
    rm -rf ${SWIFT_BUILDDIR}/lib/swift/linux/libBlocksRuntime.so
    rm -rf ${SWIFT_BUILDDIR}/lib/swift/linux/libdispatch.so 
    rm -rf ${SWIFT_BUILDDIR}/lib/swift/linux/${SWIFT_TARGET_ARCH}/*.so
    # remove some dirs from /usr/lib (we don't include them in any packages) 
    rm -rf ${SWIFT_BUILDDIR}/lib//swift/clang
    rm -rf ${SWIFT_BUILDDIR}/lib//swift/FrameworkABIBaseline
    # remove /usr/share (we don't include it in any packages) 
    rm -rf ${SWIFT_BUILDDIR}/share
    # remove /usr/bin (we don't include it in any packages)
    rm -rf ${SWIFT_BUILDDIR}/bin
}

do_install() {
    install -d ${D}${libdir}
    cp -rf ${SWIFT_BUILDDIR}/lib/swift ${D}${libdir}/
}

FILES_${PN} = "${libdir}/swift/*"
INSANE_SKIP_${PN} = "file-rdeps"
do_package_qa[noexec] = "1"

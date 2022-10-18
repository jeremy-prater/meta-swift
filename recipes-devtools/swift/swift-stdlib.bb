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

SWIFT_GGC_VERSION = "9.3.0"

EXTRA_INCLUDE_FLAGS = "\
    -I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_GGC_VERSION}/${TARGET_SYS} \
    -I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_GGC_VERSION} \
    -I${STAGING_DIR_TARGET}"

TARGET_LDFLAGS += "-w -fuse-ld=lld -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/current"

HOST_SWIFT_SUPPORT_DIR = "/tmp/swift-stdlib-yocto"
SWIFT_CMAKE_TOOLCHAIN_FILE = "${HOST_SWIFT_SUPPORT_DIR}/linux-${SWIFT_TARGET_ARCH}-toolchain.cmake"
SWIFT_CONFIGURE_CMAKE_SCRIPT="${WORKDIR}/cmake-configure-swift-stdlib.sh"
SWIFT_C_FLAGS = "-w -fuse-ld=lld -target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GGC_VERSION} -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GGC_VERSION} -I${STAGING_DIR_TARGET}/usr/include ${EXTRA_INCLUDE_FLAGS}"
SWIFT_C_LINK_FLAGS = "-target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} ${EXTRA_INCLUDE_FLAGS}"
SWIFT_CXX_FLAGS = "-w -fuse-ld=lld -target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GGC_VERSION} -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GGC_VERSION} -I${STAGING_DIR_TARGET}/usr/include -B${STAGING_DIR_TARGET}/usr/lib ${EXTRA_INCLUDE_FLAGS}"
SWIFT_CXX_LINK_FLAGS = "-target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} ${EXTRA_INCLUDE_FLAGS}"

SWIFT_CMAKE_TOOLCHAIN = "set(CMAKE_SYSTEM_NAME Linux) \
\nset(CMAKE_C_COMPILER ${STAGING_DIR_NATIVE}/opt/usr/bin/clang) \
\nset(CMAKE_CXX_COMPILER ${STAGING_DIR_NATIVE}/opt/usr/bin/clang++) \
\nset(CMAKE_C_FLAGS \"${SWIFT_C_FLAGS}\") \
\nset(CMAKE_C_LINK_FLAGS \"${SWIFT_C_LINK_FLAGS}\") \
\nset(CMAKE_CXX_FLAGS \"${SWIFT_CXX_FLAGS}\") \
\nset(CMAKE_CXX_LINK_FLAGS \"${SWIFT_CXX_LINK_FLAGS}\") \
\nset(SWIFT_USE_LINKER lld) \
\nset(LLVM_USE_LINKER lld) \
\nset(LLVM_DIR ${HOST_LLVM_PATH}/lib/cmake/llvm) \
\nset(LLVM_BUILD_LIBRARY_DIR ${HOST_LLVM_PATH}) \
\nset(LLVM_TEMPORARILY_ALLOW_OLD_TOOLCHAIN ON) \
\nset(SWIFT_INCLUDE_TOOLS OFF) \
\nset(SWIFT_BUILD_RUNTIME_WITH_HOST_COMPILER ON) \
\nset(SWIFT_PREBUILT_CLANG ON) \
\nset(SWIFT_NATIVE_CLANG_TOOLS_PATH ${STAGING_DIR_NATIVE}/opt/usr/bin) \
\nset(SWIFT_NATIVE_LLVM_TOOLS_PATH ${STAGING_DIR_NATIVE}/opt/usr/bin) \
\nset(SWIFT_NATIVE_SWIFT_TOOLS_PATH ${STAGING_DIR_NATIVE}/opt/usr/bin) \
\nset(SWIFT_BUILD_AST_ANALYZER OFF) \
\nset(SWIFT_BUILD_DYNAMIC_SDK_OVERLAY ON) \
\nset(SWIFT_BUILD_DYNAMIC_STDLIB ON) \
\nset(SWIFT_BUILD_REMOTE_MIRROR OFF) \
\nset(SWIFT_BUILD_SOURCEKIT OFF) \
\nset(SWIFT_BUILD_STDLIB_EXTRA_TOOLCHAIN_CONTENT OFF) \
\nset(SWIFT_BUILD_SYNTAXPARSERLIB OFF) \
\nset(SWIFT_BUILD_REMOTE_MIRROR OFF) \
\nset(SWIFT_ENABLE_SOURCEKIT_TESTS OFF) \
\nset(SWIFT_INCLUDE_DOCS OFF) \
\nset(SWIFT_INCLUDE_TOOLS OFF) \
\nset(SWIFT_INCLUDE_TESTS OFF) \
\nset(SWIFT_LIBRARY_EVOLUTION 0) \
\nset(SWIFT_RUNTIME_OS_VERSIONING OFF) \
\nset(SWIFT_HOST_VARIANT_ARCH ${SWIFT_TARGET_ARCH}) \
\nset(SWIFT_SDKS LINUX) \
\nset(SWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_PATH ${STAGING_DIR_TARGET} ) \
\nset(SWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_LIBC_INCLUDE_DIRECTORY ${STAGING_DIR_TARGET}/usr/include ) \
\nset(SWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_LIBC_ARCHITECTURE_INCLUDE_DIRECTORY ${STAGING_DIR_TARGET}/usr/include) \
\nset(SWIFT_LINUX_${SWIFT_TARGET_ARCH}_ICU_I18N ${STAGING_DIR_TARGET}/usr/lib/libicui18n.so) \
\nset(SWIFT_LINUX_${SWIFT_TARGET_ARCH}_ICU_UC ${STAGING_DIR_TARGET}/usr/lib/libicuuc.so) \
\nset(ZLIB_LIBRARY ${STAGING_DIR_TARGET}/usr/lib/libz.so) \
\nset(ICU_I18N_LIBRARIES ${STAGING_DIR_TARGET}/usr/lib/libicui18n.so) \
\nset(ICU_UC_LIBRARIES ${STAGING_DIR_TARGET}/usr/lib/libicuuc.so) \
\nset(SWIFT_PATH_TO_LIBDISPATCH_SOURCE ${WORKDIR}/libdispatch) \
\nset(SWIFT_ENABLE_EXPERIMENTAL_CONCURRENCY ON) \
"

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

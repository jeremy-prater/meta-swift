SUMMARY = "Swift standard library"
HOMEPAGE = "https://swift.org/"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_URI = "https://github.com/apple/swift/archive/swift-${PV}-RELEASE.tar.gz;destsuffix=swift \
        file://Float16.patch \
        file://0001-Fix-refcount.patch \
        file://fix_modulemap.sh \
        "
SRC_URI += "git://github.com/apple/swift-corelibs-libdispatch.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1;destsuffix=libdispatch" 
SRC_URI[sha256sum] = "39e4e2b7343756e26627b945a384e1b828e38778b34cc5b0f3ecc23f18d22fd6"

S = "${WORKDIR}/swift-swift-${PV}-RELEASE"
SWIFT_BUILDDIR = "${S}/build"
DEPENDS = "gcc-runtime python3-native icu ncurses"
DEPENDS_append += " swift-native libgcc gcc glibc "

inherit swift-cmake-base

################################################################################
# NOTE: The host running bitbake must have llvm available and must define      #
# HOST_LLVM_PATH as the path to the LLVM installation on the host.             #
# For example:                                                                 #
#                                                                              #
# HOST_LLVM_PATH = "/usr/lib/llvm-12"                                          #
#                                                                              #
################################################################################

SWIFT_GGC_VERSION = "9.3.0"

EXTRA_INCLUDE_FLAGS = "\
    -I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_GGC_VERSION}/arm-poky-linux-gnueabi \
    -I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_GGC_VERSION} \
    -I${STAGING_DIR_TARGET}"

TARGET_LDFLAGS += "-w -fuse-ld=lld -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/current"
TARGET_LDFLAGS += "-latomic"

SWIFT_TARGET_ARCH = "armv7"
SWIFT_TARGET_NAME = "armv7-unknown-linux-gnueabihf"
SWIFT_C_FLAGS = "-w -fuse-ld=lld -target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GGC_VERSION} -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GGC_VERSION} -I${STAGING_DIR_TARGET}/usr/include ${EXTRA_INCLUDE_FLAGS}"
SWIFT_C_LINK_FLAGS = "-target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} ${EXTRA_INCLUDE_FLAGS}"
SWIFT_CXX_FLAGS = "-w -fuse-ld=lld -target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GGC_VERSION} -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GGC_VERSION} -I${STAGING_DIR_TARGET}/usr/include -B${STAGING_DIR_TARGET}/usr/lib ${EXTRA_INCLUDE_FLAGS}"
SWIFT_CXX_LINK_FLAGS = "-target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} ${EXTRA_INCLUDE_FLAGS}"
SWIFT_STDLIB_FLAGS = ""

CC = "${STAGING_DIR_NATIVE}/opt/usr/bin/clang"
CFLAGS = "${SWIFT_C_FLAGS}"
CCLD = "${SWIFT_C_LINK_FLAGS}"

CXX = "${STAGING_DIR_NATIVE}/opt/usr/bin/clang++"
CXXFLAGS = "${SWIFT_CXX_FLAGS}"
CPP = "${STAGING_DIR_NATIVE}/opt/usr/bin/clang++"
CPPFLAGS = "${SWIFT_CXX_FLAGS}"

OECMAKE_remove += "CMAKE_C_COMPILER"
OECMAKE_remove += "CMAKE_C_FLAGS"
OECMAKE_remove += "CMAKE_C_LINK_FLAGS"
OECMAKE_remove += "CMAKE_CXX_COMPILER"
OECMAKE_remove += "CMAKE_CXX_FLAGS"
OECMAKE_remove += "CMAKE_CXX_LINK_FLAGS"
OECMAKE_remove += "CMAKE_TOOLCHAIN_FILE"

EXTRA_OECMAKE += " -DCMAKE_C_COMPILER=${STAGING_DIR_NATIVE}/opt/usr/bin/clang"
EXTRA_OECMAKE += " -DCMAKE_C_FLAGS='${SWIFT_C_FLAGS}'"
EXTRA_OECMAKE += " -DCMAKE_C_LINK_FLAGS='${SWIFT_C_LINK_FLAGS}'"

EXTRA_OECMAKE += " -DCMAKE_CXX_COMPILER=${STAGING_DIR_NATIVE}/opt/usr/bin/clang++"
EXTRA_OECMAKE += " -DCMAKE_CXX_FLAGS='${SWIFT_CXX_FLAGS}'"
EXTRA_OECMAKE += " -DCMAKE_CXX_LINK_FLAGS='${SWIFT_CXX_LINK_FLAGS}'"

EXTRA_OECMAKE += " -DCMAKE_Swift_FLAGS_DEBUG=${SWIFT_STDLIB_FLAGS}"
EXTRA_OECMAKE += " -DCMAKE_Swift_FLAGS_RELEASE=${SWIFT_STDLIB_FLAGS}"
EXTRA_OECMAKE += " -DCMAKE_Swift_FLAGS_RELWITHDEBINFO=${SWIFT_STDLIB_FLAGS}"

EXTRA_OECMAKE += " -DSWIFT_PATH_TO_LIBDISPATCH_SOURCE=${WORKDIR}/libdispatch"
EXTRA_OECMAKE += " -DSWIFT_BUILD_RUNTIME_WITH_HOST_COMPILER=ON"
EXTRA_OECMAKE += " -DSWIFT_NATIVE_CLANG_TOOLS_PATH=${STAGING_DIR_NATIVE}/opt/usr/bin"
EXTRA_OECMAKE += " -DSWIFT_NATIVE_SWIFT_TOOLS_PATH=${STAGING_DIR_NATIVE}/opt/usr/bin"

EXTRA_OECMAKE += " -DSWIFT_ENABLE_EXPERIMENTAL_CONCURRENCY=ON"
EXTRA_OECMAKE += " -DSWIFT_BUILD_AST_ANALYZER=OFF"
EXTRA_OECMAKE += " -DSWIFT_BUILD_DYNAMIC_SDK_OVERLAY=ON"
EXTRA_OECMAKE += " -DSWIFT_BUILD_DYNAMIC_STDLIB=ON"
EXTRA_OECMAKE += " -DSWIFT_BUILD_REMOTE_MIRROR=OFF"
EXTRA_OECMAKE += " -DSWIFT_BUILD_SOURCEKIT=OFF"
EXTRA_OECMAKE += " -DSWIFT_BUILD_STDLIB_EXTRA_TOOLCHAIN_CONTENT=OFF"
EXTRA_OECMAKE += " -DSWIFT_BUILD_SYNTAXPARSERLIB=OFF"
EXTRA_OECMAKE += " -DSWIFT_ENABLE_SOURCEKIT_TESTS=OFF"
EXTRA_OECMAKE += " -DSWIFT_INCLUDE_DOCS=OFF"
EXTRA_OECMAKE += " -DSWIFT_INCLUDE_TOOLS=OFF"
EXTRA_OECMAKE += " -DSWIFT_INCLUDE_TESTS=OFF"

EXTRA_OECMAKE += " -DSWIFT_HOST_VARIANT_ARCH=armv7"
EXTRA_OECMAKE += " -DSWIFT_SDK_LINUX_ARCH_armv7_PATH=${STAGING_DIR_TARGET}"
EXTRA_OECMAKE += " -DSWIFT_SDK_LINUX_ARCH_armv7_LIBC_INCLUDE_DIRECTORY=${STAGING_DIR_TARGET}/usr/include"
EXTRA_OECMAKE += " -DSWIFT_SDK_LINUX_ARCH_armv7_LIBC_ARCHITECTURE_INCLUDE_DIRECTORY=${STAGING_DIR_TARGET}/usr/include"

do_install_append() {
    # remove some dirs from /usr/lib (we don't include them in any packages) 
    rm -r ${D}${libdir}/swift/clang
    rm -r ${D}${libdir}/swift/FrameworkABIBaseline
    # remove /usr/share (we don't include it in any packages) 
    rm -r ${D}${datadir}
    # remove /usr/bin (we don't include it in any packages)
    rm -r ${D}${bindir}
    # remove Swift static libs
    rm -rf ${D}${libdir}/swift_static
    # remove Dispatch (it will be built by another package)
    rm -rf ${SWIFT_BUILDDIR}/lib/swift/linux/libBlocksRuntime.so
    rm -rf ${SWIFT_BUILDDIR}/lib/swift/linux/libdispatch.so 
}

#FILES_${PN} = "${libdir}/swift/linux/*.so"
FILES_${PN} = "${libdir}/swift/*"
INSANE_SKIP_${PN} = "file-rdeps"

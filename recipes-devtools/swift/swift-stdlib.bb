SUMMARY = "Swift standard library"
HOMEPAGE = "https://swift.org/"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_URI = "git://github.com/apple/swift.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1 \
        file://0001-Float16.patch \
        file://fix_modulemap.sh \
        "
SRC_URI += "git://github.com/apple/swift-corelibs-libdispatch.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1;destsuffix=libdispatch" 

S = "${WORKDIR}/git"
SWIFT_BUILDDIR = "${S}/build"
DEPENDS = "gcc-runtime python3-native icu ncurses"
DEPENDS += " swift-native swift-llvm-native libgcc gcc glibc libxml2"

inherit swift-cmake-base

SWIFT_GGC_VERSION = "9.3.0"

EXTRA_INCLUDE_FLAGS = "\
    -I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_GGC_VERSION}/arm-poky-linux-gnueabi \
    -I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_GGC_VERSION} \
    -I${STAGING_DIR_TARGET}"

SWIFT_LDFLAGS += "-w -fuse-ld=lld -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/current"
SWIFT_LDFLAGS += "-latomic"

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

LDFLAGS = "${SWIFT_LDFLAGS}"
FC = "${SWIFT_LDFLAGS}"

OECMAKE_C_FLAGS = "${CFLAGS}"
OECMAKE_CXX_FLAGS = "${CXXFLAGS}"
OECMAKE_C_FLAGS_RELEASE = "-DNDEBUG"
OECMAKE_CXX_FLAGS_RELEASE = "-DNDEBUG"
OECMAKE_C_LINK_FLAGS = "${SWIFT_C_LINK_FLAGS}"
OECMAKE_CXX_LINK_FLAGS = "${SWIFT_CXX_LINK_FLAGS}"

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

EXTRA_OECMAKE += " -DCMAKE_Swift_FLAGS=${SWIFT_STDLIB_FLAGS}"
EXTRA_OECMAKE += ' -DCMAKE_Swift_FLAGS_DEBUG=""'
EXTRA_OECMAKE += ' -DCMAKE_Swift_FLAGS_RELEASE=""'
EXTRA_OECMAKE += ' -DCMAKE_Swift_FLAGS_RELWITHDEBINFO=""'

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

EXTRA_OECMAKE += " -DSWIFT_HOST_VARIANT_ARCH=${SWIFT_TARGET_ARCH}"
EXTRA_OECMAKE += " -DSWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_PATH=${STAGING_DIR_TARGET}"
EXTRA_OECMAKE += " -DSWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_LIBC_INCLUDE_DIRECTORY=${STAGING_DIR_TARGET}/usr/include"
EXTRA_OECMAKE += " -DSWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_LIBC_ARCHITECTURE_INCLUDE_DIRECTORY=${STAGING_DIR_TARGET}/usr/include"


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

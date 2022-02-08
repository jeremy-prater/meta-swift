SUMMARY = "Swift standard library"
HOMEPAGE = "https://swift.org/"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_URI = "https://github.com/apple/swift/archive/swift-${PV}-RELEASE.tar.gz;destsuffix=swift"
SRC_URI += "git://github.com/apple/swift-corelibs-libdispatch.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1;destsuffix=libdispatch" 
SRC_URI[sha256sum] = "0046ecab640475441251b1cceb3dd167a4c7729852104d7675bdbd75fced6b82"

S = "${WORKDIR}/swift-swift-${PV}-RELEASE"
DEPENDS = "gcc-runtime python3-native icu ncurses"

inherit swift-cmake-base

################################################################################
# NOTE: The host running bitbake must have llvm available and must define      #
# HOST_LLVM_PATH as the path to the LLVM installation on the host.             #
# For example:                                                                 #
#                                                                              #
# HOST_LLVM_PATH = "/usr/lib/llvm-12"                                          #
#                                                                              #
################################################################################

HOST_LLVM_PATH = "/usr/lib/llvm-12"
EXTRA_OECMAKE += " -DSWIFT_PATH_TO_LIBDISPATCH_SOURCE=${WORKDIR}/libdispatch"

EXTRA_OECMAKE += " -DLLVM_DIR=${HOST_LLVM_PATH}/cmake"
EXTRA_OECMAKE += " -DLLVM_BUILD_LIBRARY_DIR=${HOST_LLVM_PATH}/lib"
EXTRA_OECMAKE += " -DLLVM_MAIN_INCLUDE_DIR=${HOST_LLVM_PATH}/include"

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
EXTRA_OECMAKE += "-DSWIFT_SDK_LINUX_ARCH_armv7_LIBC_INCLUDE_DIRECTORY=${STAGING_DIR_TARGET}/usr/include"
EXTRA_OECMAKE += "-DSWIFT_SDK_LINUX_ARCH_armv7_LIBC_ARCHITECTURE_INCLUDE_DIRECTORY=${STAGING_DIR_TARGET}/usr/include"

EXTRA_INCLUDE_FLAGS = "\
    -I${STAGING_DIR_TARGET}/usr/include/c++/current/arm-poky-linux-gnueabi \
    -I${STAGING_DIR_TARGET}/usr/include/c++/current \
    -I${STAGING_DIR_TARGET}"

TARGET_LDFLAGS += "-latomic"

do_install_append() {
    # remove some dirs from /usr/lib (we don't include them in any packages) 
    rm -r ${D}${libdir}/swift/clang
    rm -r ${D}${libdir}/swift/FrameworkABIBaseline

    # remove /usr/share (we don't include it in any packages) 
    rm -r ${D}${datadir}

    # remove /usr/bin (we don't include it in any packages)
    rm -r ${D}${bindir}

    rm -rf ${D}${libdir}/swift_static
}

FILES_${PN} = "${libdir}/swift/linux/*.so \
"

FILES_${PN}-dev = "${libdir}/swift/* \
"

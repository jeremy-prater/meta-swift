SUMMARY = "Swift standard library"
HOMEPAGE = "https://swift.org/"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${SOURCE_ROOT}/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_URI = "https://github.com/apple/swift/archive/swift-${PV}-RELEASE.tar.gz \
           file://fix_modulemap.sh \
           file://0001-Require-python3-rather-than-python2.patch \
           file://0001-Add-Wno-gnu-include-next-to-swift-reflection-test.patch \
           "
SRC_URI[sha256sum] = "f9e5bd81441c4ec13dd9ea290e2d7b8fe9b30ef66ad68947481022ea5179f83a"

SOURCE_ROOT = "${WORKDIR}/swift-swift-${PV}-RELEASE"
S = "${SOURCE_ROOT}"
DEPENDS = "gcc-runtime python3-native icu ncurses"

inherit swift-cmake-base

################################################################################
# NOTE: The host running bitbake must have llvm available and must define      #
# HOST_LLVM_PATH as the path to the LLVM installation on the host.             #
# For example:                                                                 #
#                                                                              #
# HOST_LLVM_PATH = "/usr/lib/llvm-10"                                          #
#                                                                              #
################################################################################
EXTRA_OECMAKE += " -DLLVM_DIR=${HOST_LLVM_PATH}/cmake"
EXTRA_OECMAKE += " -DLLVM_BUILD_LIBRARY_DIR=${HOST_LLVM_PATH}/lib"
EXTRA_OECMAKE += " -DLLVM_MAIN_INCLUDE_DIR=${HOST_LLVM_PATH}/include"

EXTRA_OECMAKE += " -DSWIFT_BUILD_RUNTIME_WITH_HOST_COMPILER=ON"
EXTRA_OECMAKE += " -DSWIFT_NATIVE_CLANG_TOOLS_PATH=${STAGING_DIR_NATIVE}/usr/bin"
EXTRA_OECMAKE += " -DSWIFT_NATIVE_SWIFT_TOOLS_PATH=${STAGING_DIR_NATIVE}/usr/bin"

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
    -I${STAGING_DIR_TARGET}/usr/include/c++/${GCC_VERSION}/arm-poky-linux-gnueabi \
    -I${STAGING_DIR_TARGET}/usr/include/c++/${GCC_VERSION} \
    -I${STAGING_DIR_TARGET}"

do_install_append() {
    ${WORKDIR}/fix_modulemap.sh ${D}${libdir}/swift/linux/armv7/glibc.modulemap

    rm ${D}${libdir}/swift/linux/armv7/glibc.modulemap_orig_*

    # remove some dirs from /usr/lib (we don't include them in any packages) 
    rm -r ${D}${libdir}/swift/clang
    rm -r ${D}${libdir}/swift/FrameworkABIBaseline

    # remove /usr/share (we don't include it in any packages) 
    rm -r ${D}${datadir}

    # remove /usr/bin (we don't include it in any packages)
    rm -r ${D}${bindir}
}

FILES_${PN} = "${libdir}/swift/linux/libswiftCore.so \
               ${libdir}/swift/linux/libswiftGlibc.so \
               ${libdir}/swift/linux/libswiftRemoteMirror.so \
               ${libdir}/swift/linux/libswiftSwiftOnoneSupport.so \
"

FILES_${PN}-dev = "${libdir}/swift/shims/* \
                   ${libdir}/swift/linux/armv7/glibc.modulemap \
                   ${libdir}/swift/linux/armv7/private_includes/* \
                   ${libdir}/swift/linux/armv7/Glibc.swiftmodule \
                   ${libdir}/swift/linux/armv7/Glibc.swiftinterface \
                   ${libdir}/swift/linux/armv7/Swift.swiftmodule \
                   ${libdir}/swift/linux/armv7/Swift.swiftinterface \
                   ${libdir}/swift/linux/armv7/SwiftOnoneSupport.swiftmodule \
                   ${libdir}/swift/linux/armv7/SwiftOnoneSupport.swiftinterface \
                   ${libdir}/swift/linux/armv7/swiftrt.o \
"

FILES_${PN}-doc = "${libdir}/swift/linux/armv7/Swift.swiftdoc \
                   ${libdir}/swift/linux/armv7/Glibc.swiftdoc \
                   ${libdir}/swift/linux/armv7/SwiftOnoneSupport.swiftdoc \
"

SUMMARY = "Swift standard library"
HOMEPAGE = "https://swift.org/"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${SOURCE_ROOT}/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_URI = "https://github.com/apple/swift/archive/swift-${PV}-RELEASE.tar.gz \
           file://0001-Require-python3-rather-than-python2.patch \
           file://0001-Add-Wno-gnu-include-next-to-swift-reflection-test.patch \
           "
SRC_URI[sha256sum] = "f9e5bd81441c4ec13dd9ea290e2d7b8fe9b30ef66ad68947481022ea5179f83a"

SOURCE_ROOT = "${WORKDIR}/swift-swift-${PV}-RELEASE"
S = "${SOURCE_ROOT}"
DEPENDS = "swift-native libgcc glibc gcc-runtime python3-native icu apple-llvm"

inherit cmake

HOST_CC_ARCH_prepend = "-target armv7-unknown-linux-gnueabih"

################################################################################
# NOTE: The host running bitbake must have lld available and the following     #
# must be added to the local.conf file:                                        #
#                                                                              #
# HOSTTOOLS += "ld.lld lld"                                                    #
#                                                                              #
################################################################################

# Use lld (see note above)
TARGET_LDFLAGS += "-fuse-ld=lld"

# Use Apple's provided clang (it understands Apple's custom compiler flags)
# Made available via swift-native package.
OECMAKE_C_COMPILER = "clang"
OECMAKE_CXX_COMPILER = "clang++"

# TODO: remove machine specific paths
# Project specific settings for cross compiling stdlib
EXTRA_OECMAKE += " -DLLVM_DIR=/usr/lib/llvm-10/cmake"
EXTRA_OECMAKE += " -DLLVM_BUILD_LIBRARY_DIR=/usr/lib/llvm-10/lib"
EXTRA_OECMAKE += " -DLLVM_MAIN_INCLUDE_DIR=/usr/lib/llvm-10/include"

EXTRA_OECMAKE += " -DSWIFT_BUILD_RUNTIME_WITH_HOST_COMPILER=ON"
EXTRA_OECMAKE += " -DSWIFT_NATIVE_CLANG_TOOLS_PATH=/home/kevin/Downloads/swift-5.3-RELEASE-ubuntu20.04/usr/bin"
EXTRA_OECMAKE += " -DSWIFT_NATIVE_SWIFT_TOOLS_PATH=/home/kevin/Downloads/swift-5.3-RELEASE-ubuntu20.04/usr/bin"

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

EXTRA_OECMAKE += " -DSWIFT_HOST_VARIANT_ARCH=armv7"

EXTRA_OECMAKE += " -DSWIFT_SDK_LINUX_ARCH_armv7_PATH=${WORKDIR}/recipe-sysroot"

# Point clang to where the C++ runtime is for our target arch
RUNTIME_FLAGS = "-B${WORKDIR}/recipe-sysroot/usr/lib/${TARGET_SYS}/9.3.0 -I${WORKDIR}/recipe-sysroot/usr/include/c++/9.3.0"
TARGET_LDFLAGS += "-L${WORKDIR}/recipe-sysroot/usr/lib/${TARGET_SYS}/9.3.0"

EXTRA_INCLUDE_FLAGS = "-I${WORKDIR}/recipe-sysroot/usr/include/c++/9.3.0/arm-poky-linux-gnueabi -I${WORKDIR}/recipes-sysroot"
OECMAKE_C_FLAGS += "${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"
OECMAKE_CXX_FLAGS += "${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"

#do_compile () {
#    oe_runmake swiftImageRegistrationObjectELF-linux-armv7
#    oe_runmake lib-swift-linux-armv7-swiftrt.o
#    oe_runmake swift-stdlib
#}

do_install_append() {
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

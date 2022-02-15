SUMMARY = "Swift standard library"
HOMEPAGE = "https://swift.org/"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_URI = "https://github.com/apple/swift/archive/swift-${PV}-RELEASE.tar.gz;destsuffix=swift \
        file://Float16.patch \
        file://0001-Fix-refcount.patch \
        "
SRC_URI += "git://github.com/apple/swift-corelibs-libdispatch.git;protocol=https;tag=swift-${PV}-RELEASE;nobranch=1;destsuffix=libdispatch" 
SRC_URI[sha256sum] = "41c926ae261a2756fe5ff761927aafe297105dc62f676a27c3da477f13251888"

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
EXTRA_OECMAKE += "-DSWIFT_SDK_LINUX_ARCH_armv7_LIBC_INCLUDE_DIRECTORY=${STAGING_DIR_TARGET}/usr/include"
EXTRA_OECMAKE += "-DSWIFT_SDK_LINUX_ARCH_armv7_LIBC_ARCHITECTURE_INCLUDE_DIRECTORY=${STAGING_DIR_TARGET}/usr/include"

EXTRA_INCLUDE_FLAGS = "\
    -I${STAGING_DIR_TARGET}/usr/include/c++/current/arm-poky-linux-gnueabi \
    -I${STAGING_DIR_TARGET}/usr/include/c++/current \
    -I${STAGING_DIR_TARGET}"

TARGET_LDFLAGS += "-latomic"

SWIFT_BUILDDIR = "${S}/build"
SWIFT_TARGET_ARCH = "armv7"
SWIFT_TARGET_NAME = "armv7-unknown-linux-gnueabihf"
HOST_SWIFT_SUPPORT_DIR = "/tmp/swift-stdlib-yocto"
SWIFT_CMAKE_TOOLCHAIN_FILE = "${HOST_SWIFT_SUPPORT_DIR}/linux-${SWIFT_TARGET_ARCH}-toolchain.cmake"
SWIFT_C_FLAGS = "-w -fuse-ld=lld -target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} -I${STAGING_DIR_TARGET}/usr/include -B${STAGING_DIR_TARGET}/usr/lib"
SWIFT_C_LINK_FLAGS = "-target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET}"
SWIFT_CXX_FLAGS = "-w -fuse-ld=lld -target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} -I${STAGING_DIR_TARGET}/usr/include -B${STAGING_DIR_TARGET}/usr/lib"
SWIFT_CXX_LINK_FLAGS = "-target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET}"

SWIFT_CMAKE_TOOLCHAIN = "set(CMAKE_SYSTEM_NAME Linux) \
\n set(CMAKE_C_COMPILER ${STAGING_DIR_NATIVE}/opt/usr/bin/clang) \
\n set(CMAKE_CXX_COMPILER ${STAGING_DIR_NATIVE}/opt/usr/bin/clang++) \
\n set(CMAKE_C_FLAGS \"${SWIFT_C_FLAGS}\") \
\n set(CMAKE_C_LINK_FLAGS \"${SWIFT_C_LINK_FLAGS}\") \
\n set(CMAKE_CXX_FLAGS \"${SWIFT_CXX_FLAGS}\") \
\n set(CMAKE_CXX_LINK_FLAGS \"${SWIFT_CXX_LINK_FLAGS}\") \
\n set(SWIFT_USE_LINKER lld) \
\n set(LLVM_USE_LINKER lld) \
\n set(LLVM_DIR ${HOST_LLVM_PATH}/lib/cmake/llvm) \
\n set(LLVM_BUILD_LIBRARY_DIR ${HOST_LLVM_PATH}) \
\n set(LLVM_TEMPORARILY_ALLOW_OLD_TOOLCHAIN ON) \
\n set(SWIFT_INCLUDE_TOOLS OFF) \
\n set(SWIFT_BUILD_RUNTIME_WITH_HOST_COMPILER ON) \
\n set(SWIFT_PREBUILT_CLANG ON) \
\n set(SWIFT_NATIVE_CLANG_TOOLS_PATH ${STAGING_DIR_NATIVE}/opt/usr/bin) \
\n set(SWIFT_NATIVE_LLVM_TOOLS_PATH ${STAGING_DIR_NATIVE}/opt/usr/bin) \
\n set(SWIFT_NATIVE_SWIFT_TOOLS_PATH ${STAGING_DIR_NATIVE}/opt/usr/bin) \
\n set(SWIFT_BUILD_AST_ANALYZER OFF) \
\n set(SWIFT_BUILD_DYNAMIC_SDK_OVERLAY ON) \
\n set(SWIFT_BUILD_DYNAMIC_STDLIB ON) \
\n set(SWIFT_BUILD_REMOTE_MIRROR OFF) \
\n set(SWIFT_BUILD_SOURCEKIT OFF) \
\n set(SWIFT_BUILD_STDLIB_EXTRA_TOOLCHAIN_CONTENT OFF) \
\n set(SWIFT_BUILD_SYNTAXPARSERLIB OFF) \
\n set(SWIFT_BUILD_REMOTE_MIRROR OFF) \
\n set(SWIFT_ENABLE_SOURCEKIT_TESTS OFF) \
\n set(SWIFT_INCLUDE_DOCS OFF) \
\n set(SWIFT_INCLUDE_TOOLS OFF) \
\n set(SWIFT_INCLUDE_TESTS OFF) \
\n set(SWIFT_LIBRARY_EVOLUTION 0) \
\n set(SWIFT_RUNTIME_OS_VERSIONING OFF) \
\n set(SWIFT_HOST_VARIANT_ARCH ${SWIFT_TARGET_ARCH}) \
\n set(SWIFT_SDKS LINUX) \
\n set(SWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_PATH ${STAGING_DIR_TARGET} ) \
\n set(SWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_LIBC_INCLUDE_DIRECTORY ${STAGING_DIR_TARGET}/usr/include ) \
\n set(SWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_LIBC_ARCHITECTURE_INCLUDE_DIRECTORY ${STAGING_DIR_TARGET}/usr/include) \
\n set(SWIFT_LINUX_${SWIFT_TARGET_ARCH}_ICU_I18N ${STAGING_DIR_TARGET}/usr/lib/libicui18n.so) \
\n set(SWIFT_LINUX_${SWIFT_TARGET_ARCH}_ICU_UC ${STAGING_DIR_TARGET}/usr/lib/libicuuc.so) \
\n set(ICU_I18N_LIBRARIES ${STAGING_DIR_TARGET}/usr/lib/libicui18n.so) \
\n set(ICU_UC_LIBRARIES ${STAGING_DIR_TARGET}/usr/lib/libicuuc.so) \
\n set(LibRT_LIBRARIES ${STAGING_DIR_TARGET}/usr/lib/librt.a) \
\n set(ZLIB_LIBRARY ${STAGING_DIR_TARGET}/usr/lib/libz.so) \
\n set(SWIFT_PATH_TO_LIBDISPATCH_SOURCE ${WORKDIR}/libdispatch) \
\n set(SWIFT_ENABLE_EXPERIMENTAL_CONCURRENCY ON) \
"

do_configure() {
    mkdir -p ${HOST_SWIFT_SUPPORT_DIR}
	rm -f ${SWIFT_CMAKE_TOOLCHAIN_FILE}
	touch ${SWIFT_CMAKE_TOOLCHAIN_FILE}
    printf "${SWIFT_CMAKE_TOOLCHAIN}" >> ${SWIFT_CMAKE_TOOLCHAIN_FILE}
	rm -rf ${SWIFT_BUILDDIR}
	mkdir -p ${SWIFT_BUILDDIR}
	cd ${SWIFT_BUILDDIR} && rm -f CMakeCache.txt
	cd ${SWIFT_BUILDDIR} && PATH="${PATH}:/usr/bin" LIBS="-latomic" cmake -S ${S} -B ${SWIFT_BUILDDIR} -G Ninja \
		-DCMAKE_INSTALL_PREFIX="/usr" \
		-DCMAKE_COLOR_MAKEFILE=OFF \
		-DBUILD_DOC=OFF \
		-DBUILD_DOCS=OFF \
		-DBUILD_EXAMPLE=OFF \
		-DBUILD_EXAMPLES=OFF \
		-DBUILD_TEST=OFF \
		-DBUILD_TESTS=OFF \
		-DBUILD_TESTING=OFF \
		-DBUILD_SHARED_LIBS=ON \
		-DCMAKE_CROSSCOMPILING=ON \
        -DCMAKE_TOOLCHAIN_FILE=${SWIFT_CMAKE_TOOLCHAIN_FILE} \
		-DCMAKE_BUILD_TYPE=Release \
        -DCMAKE_SYSTEM_NAME=Linux \
	    -DCMAKE_C_COMPILER=${STAGING_DIR_NATIVE}/opt/usr/bin/clang \
        -DCMAKE_CXX_COMPILER=${STAGING_DIR_NATIVE}/opt/usr/bin/clang++ \
        -DCMAKE_C_FLAGS="-target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} -B${STAGING_DIR_TARGET}/usr/lib ${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}" \
	    -DCMAKE_C_LINK_FLAGS="-target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} ${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}" \
        -DCMAKE_CXX_FLAGS="-target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} ${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}" \
        -DCMAKE_CXX_LINK_FLAGS="-target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} ${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}" \
		-DSWIFT_USE_LINKER=lld \
        -DLLVM_USE_LINKER=lld \
        -DLLVM_DIR=${HOST_LLVM_PATH}/lib/cmake/llvm \
        -DLLVM_BUILD_LIBRARY_DIR=${HOST_LLVM_PATH} \
        -DLLVM_TEMPORARILY_ALLOW_OLD_TOOLCHAIN=ON \
        -DSWIFT_PATH_TO_LIBDISPATCH_SOURCE=${WORKDIR}/libdispatch
        -DSWIFT_BUILD_RUNTIME_WITH_HOST_COMPILER=ON \
        -DSWIFT_NATIVE_CLANG_TOOLS_PATH=${STAGING_DIR_NATIVE}/opt/usr/bin \
        -DSWIFT_NATIVE_SWIFT_TOOLS_PATH=${STAGING_DIR_NATIVE}/opt/usr/bin \
        -DSWIFT_BUILD_AST_ANALYZER=OFF \
        -DSWIFT_BUILD_DYNAMIC_SDK_OVERLAY=ON \
        -DSWIFT_BUILD_DYNAMIC_STDLIB=ON \
        -DSWIFT_BUILD_REMOTE_MIRROR=OFF \
        -DSWIFT_BUILD_SOURCEKIT=OFF \
        -DSWIFT_BUILD_STDLIB_EXTRA_TOOLCHAIN_CONTENT=OFF \
        -DSWIFT_BUILD_SYNTAXPARSERLIB=OFF \
        -DSWIFT_BUILD_REMOTE_MIRROR=OFF \
        -DSWIFT_ENABLE_SOURCEKIT_TESTS=OFF \
        -DSWIFT_INCLUDE_DOCS=OFF \
        -DSWIFT_INCLUDE_TOOLS=OFF \
        -DSWIFT_INCLUDE_TESTS=OFF \
        -DSWIFT_LIBRARY_EVOLUTION=0 \
        -DSWIFT_RUNTIME_OS_VERSIONING=OFF \
        -DSWIFT_ENABLE_EXPERIMENTAL_CONCURRENCY=ON \
        -DSWIFT_HOST_VARIANT_ARCH=${SWIFT_TARGET_ARCH} \
        -DSWIFT_SDKS=LINUX \
        -DSWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_PATH=${STAGING_DIR_TARGET}  \
        -DSWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_LIBC_INCLUDE_DIRECTORY=${STAGING_DIR_TARGET}/usr/include  \
        -DSWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_LIBC_ARCHITECTURE_INCLUDE_DIRECTORY=${STAGING_DIR_TARGET}/usr/include \
        -DSWIFT_LINUX_${SWIFT_TARGET_ARCH}_ICU_I18N=${STAGING_DIR_TARGET}/usr/lib/libicui18n.so \
        -DSWIFT_LINUX_${SWIFT_TARGET_ARCH}_ICU_UC=${STAGING_DIR_TARGET}/usr/lib/libicuuc.so \
        -DICU_I18N_LIBRARIES=${STAGING_DIR_TARGET}/usr/lib/libicui18n.so \
        -DICU_UC_LIBRARIES=${STAGING_DIR_TARGET}/usr/lib/libicuuc.so
}

do_compile() {
    cd ${SWIFT_BUILDDIR} && ninja
}

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

#FILES_${PN} = "${libdir}/swift/linux/*.so"
FILES_${PN} = "${libdir}/swift/*"
INSANE_SKIP_${PN} = "file-rdeps"
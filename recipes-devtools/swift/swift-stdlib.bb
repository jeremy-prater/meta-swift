SUMMARY = "Swift"
DESCRIPTION = "The Swift programming language standard library"
HOMEPAGE = "https://github.com/swiftlang/swift"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

PACKAGES:append = " ${PN}-embedded"

require swift-version.inc
PV = "${SWIFT_VERSION}+git${SRCPV}"
SRCREV_FORMAT = "swift_libdispatch_stringproc_syntax"

SRC_URI = "\
    git://github.com/swiftlang/llvm-project.git;protocol=https;name=llvm-project;tag=${SWIFT_TAG};nobranch=1;destsuffix=llvm-project; \
    git://github.com/swiftlang/swift.git;protocol=https;name=swift;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift; \
    git://github.com/swiftlang/swift-corelibs-libdispatch.git;protocol=https;name=libdispatch;tag=${SWIFT_TAG};nobranch=1;destsuffix=libdispatch; \
    git://github.com/swiftlang/swift-experimental-string-processing.git;protocol=https;name=stringproc;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-experimental-string-processing; \
    git://github.com/swiftlang/swift-syntax.git;protocol=https;name=syntax;tag=${SWIFT_TAG};nobranch=1;destsuffix=swift-syntax; \
    file://0003-add-fix-for-metadataaccessor-abi-mismatch.patch;striplevel=1; \
    file://0004-AddSwiftStdlib-skip-empty-sysroot-injection.patch;striplevel=1; \
    "

S = "${UNPACKDIR}/swift"

SWIFT_BUILDDIR = "${S}/build"
# SWIFT_CXX_RUNTIME=llvm → pull the system libc++ (OE-core's libcxx) into the
# sysroot; default (gnu) keeps GCC's libstdc++ via gcc-runtime.
DEPENDS = "${@bb.utils.contains('SWIFT_CXX_RUNTIME', 'llvm', 'libcxx', 'gcc-runtime', d)} python3-native icu ncurses swift-native libgcc gcc glibc libxml2 libxml2-native ninja-native"

inherit swift-cmake-base

TARGET_LDFLAGS:append = " -w -fuse-ld=lld -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION}"

SWIFT_CMAKE_TOOLCHAIN_FILE = "${WORKDIR}/linux-${SWIFT_TARGET_ARCH}-toolchain.cmake"

# x86_64 + libstdc++: ClangImporter's std module build hits a
# std -> _Builtin_intrinsics -> std cycle when bits/opt_random.h pulls
# in <pmmintrin.h> (under __SSE3__) which then re-enters std via
# mm_malloc.h -> stdlib.h -> cstdlib -> bits/std_abs.h. The cycle's
# load-bearing edge is bits/std_abs.h, which the prebuilt swiftc's
# ClangImporter injects as a modular `header` into module std at
# runtime via getLibStdCxxFileMapping() -- so we cannot break the
# cycle in the source modulemap (Clang rejects header + textual header
# coexisting). Skipping pmmintrin.h via -mno-sse3 only for the
# CxxStdlib swiftmodule build sidesteps the cycle. This affects only
# Swift's C++ header parsing for that target; it does not change the
# swiftmodule's ABI or any code's target architecture.
#
# SwiftConfigureSDK.cmake seeds SWIFT_SDK_LINUX_CXX_OVERLAY_SWIFT_COMPILE_FLAGS
# with `set(... CACHE STRING)` (no FORCE), which removes any pre-existing
# normal variable -- so the value must be pre-seeded as a cache entry on the
# outer cmake command line to win. Value is a CMake list (semicolon-separated)
# and must preserve the upstream default `-Xcc --gcc-toolchain=/usr` so the
# GCC toolchain lookup still works on non-x86_64 targets.
#
# Key on TARGET_ARCH rather than a :x86-64 override: TUNE_PKGARCH for
# non-baseline tunes is "x86-64-v3" etc., and the plain tune sets it
# to "x86_64" (underscore) -- so no hyphen-form "x86-64" ever lands in
# OVERRIDES. TARGET_ARCH resolves to "x86_64" for every x86 ELF64/ILP32
# tune, which is the set that has the x86 intrinsic headers in scope.
SWIFT_CXX_OVERLAY_SWIFT_COMPILE_FLAGS = "-Xcc;--gcc-toolchain=/usr${@';-Xcc;-mno-sse3' if d.getVar('TARGET_ARCH') == 'x86_64' and d.getVar('SWIFT_CXX_RUNTIME') == 'gnu' else ''}"

SWIFT_TARGET_COMMON_FLAGS = "${TARGET_CC_ARCH} -w -fuse-ld=lld -target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION} -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_GCC_VERSION}"

SWIFT_C_FLAGS = "${SWIFT_TARGET_COMMON_FLAGS} -I${STAGING_DIR_TARGET}/usr/include ${EXTRA_INCLUDE_FLAGS}"
SWIFT_C_LINK_FLAGS = "${TARGET_LD_ARCH} -target ${SWIFT_TARGET_NAME} --sysroot ${STAGING_DIR_TARGET} ${EXTRA_INCLUDE_FLAGS}"

# Under libc++, EXTRA_CXX_INCLUDE_FLAGS must precede the C sysroot -I so
# libc++'s wrapper headers (c++/v1/{cstddef,cfloat,...}) win on search order;
# otherwise libc++'s <cstddef>/<cfloat> abort with "didn't find libc++'s <X>
# header" -- notably in the stdlib's embedded build that appends an empty
# `--sysroot=` and defeats Clang's sysroot-based libc++ auto-discovery.
# Under libstdc++, keep C sysroot first (original behavior).
SWIFT_CXX_FLAGS = "${@bb.utils.contains('SWIFT_CXX_RUNTIME', 'llvm', \
    '${SWIFT_TARGET_COMMON_FLAGS} ${EXTRA_CXX_INCLUDE_FLAGS} -I${STAGING_DIR_TARGET}/usr/include ${EXTRA_INCLUDE_FLAGS}', \
    '${SWIFT_C_FLAGS} ${EXTRA_CXX_INCLUDE_FLAGS}', d)} -Wno-invalid-constexpr"
SWIFT_CXX_LINK_FLAGS = "${SWIFT_C_LINK_FLAGS} ${EXTRA_CXX_INCLUDE_FLAGS}"

# swiftc -Xcc flags for the Swift stdlib's own C++ header parsing.
SWIFT_STDLIB_CXX_FLAGS = "${@bb.utils.contains('SWIFT_CXX_RUNTIME', 'llvm', \
    '-Xcc -stdlib=libc++ -Xcc -I${STAGING_DIR_TARGET}/usr/include/c++/v1', \
    '-I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_GCC_VERSION} -I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_GCC_VERSION}/${TARGET_SYS}', d)}"

do_fix_gcc_install_dir() {
    # symbolic links do not work, will not be found by Swift clang driver
    # this is necessary to make the libstdc++ location heuristic work, necessary for C++ interop
    (cd ${STAGING_DIR_TARGET}/usr/lib && rm -rf gcc && mkdir -p gcc && cp -rp ${TARGET_ARCH}${TARGET_VENDOR}-${TARGET_OS} gcc)
}

addtask fix_gcc_install_dir before do_configure after do_prepare_recipe_sysroot

do_configure() {
    export SDKROOT=${STAGING_DIR_TARGET}
    export LLVM_SRCDIR=${UNPACKDIR}/llvm-project
    export LLVM_BUILDDIR=${LLVM_SRCDIR}/build
    export SWIFT_SRCDIR=${S}
    export SWIFT_NATIVE_PATH=${STAGING_DIR_NATIVE}/usr/bin
    export CC=${STAGING_DIR_NATIVE}/usr/bin/clang
    export CFLAGS="${SWIFT_C_FLAGS}"
    export CCLD="${SWIFT_C_LINK_FLAGS}"
    export CXX=${STAGING_DIR_NATIVE}/usr/bin/clang++
    export CXXFLAGS="${SWIFT_CXX_FLAGS}"

    rm -rf ${LLVM_BUILDDIR}
    mkdir -p ${LLVM_BUILDDIR}

    # Configure the llvm project to get the cmake files generated, so we can point
    # LLVM_DIR to this folder
    cmake -S ${LLVM_SRCDIR}/llvm -B ${LLVM_BUILDDIR} -G Ninja \
       -DCMAKE_INSTALL_PREFIX=${STAGING_DIR_NATIVE}/usr/lib \
       -DCMAKE_C_COMPILER=${SWIFT_NATIVE_PATH}/clang \
       -DCMAKE_CXX_COMPILER=${SWIFT_NATIVE_PATH}/clang++ \
       -DLLVM_TARGETS_TO_BUILD="X86;ARM;AArch64" \
       -DLLVM_ENABLE_PROJECTS="llvm" \
       -DCMAKE_BUILD_TYPE=Release

    rm -rf ${SWIFT_BUILDDIR}
    mkdir -p ${SWIFT_BUILDDIR}

    cat <<EOF > ${SWIFT_CMAKE_TOOLCHAIN_FILE}
set(CMAKE_COLOR_MAKEFILE OFF)
set(CMAKE_CROSSCOMPILING ON)
set(CMAKE_BUILD_TYPE Release)
set(CMAKE_SYSTEM_NAME Linux)
set(CMAKE_SYSTEM_PROCESSOR ${SWIFT_TARGET_ARCH})
set(CMAKE_SYSROOT ${STAGING_DIR_TARGET})

set(CMAKE_C_COMPILER ${SWIFT_NATIVE_PATH}/clang)
set(CMAKE_C_COMPILER_TARGET ${TARGET_SYS})
set(CMAKE_C_FLAGS "${SWIFT_C_FLAGS}")
set(CMAKE_C_LINK_FLAGS "${SWIFT_C_LINK_FLAGS}")

set(CMAKE_CXX_COMPILER ${SWIFT_NATIVE_PATH}/clang++)
set(CMAKE_CXX_COMPILER_TARGET ${TARGET_SYS})
set(CMAKE_CXX_FLAGS "${SWIFT_CXX_FLAGS}")
set(CMAKE_CXX_LINK_FLAGS "${SWIFT_CXX_LINK_FLAGS}")

set(CMAKE_Swift_COMPILER ${SWIFT_NATIVE_PATH}/swiftc)
set(CMAKE_Swift_COMPILER_TARGET ${TARGET_SYS})
set(CMAKE_Swift_COMPILER_WORKS ON)

set(BUILD_DOC OFF)
set(BUILD_DOCS OFF)
set(BUILD_EXAMPLE OFF)
set(BUILD_EXAMPLES OFF)
set(BUILD_TEST OFF)
set(BUILD_TESTS OFF)
set(BUILD_TESTING OFF)
set(BUILD_SHARED_LIBS ON)

set(LLVM_USE_LINKER lld)
set(LLVM_DIR ${LLVM_BUILDDIR}/lib/cmake/llvm)
set(LLVM_BUILD_LIBRARY_DIR ${LLVM_BUILDDIR})
set(LLVM_TEMPORARILY_ALLOW_OLD_TOOLCHAIN ON)

set(SWIFT_USE_LINKER lld)
set(SWIFT_INCLUDE_TOOLS OFF)
set(SWIFT_BUILD_RUNTIME_WITH_HOST_COMPILER ON)
set(SWIFT_PREBUILT_CLANG ON)
set(SWIFT_NATIVE_CLANG_TOOLS_PATH ${SWIFT_NATIVE_PATH})
set(SWIFT_NATIVE_LLVM_TOOLS_PATH ${SWIFT_NATIVE_PATH})
set(SWIFT_NATIVE_SWIFT_TOOLS_PATH ${SWIFT_NATIVE_PATH})
set(SWIFT_BUILD_AST_ANALYZER OFF)
set(SWIFT_BUILD_DYNAMIC_SDK_OVERLAY ON)
set(SWIFT_BUILD_DYNAMIC_STDLIB ON)
set(SWIFT_BUILD_REMOTE_MIRROR OFF)
set(SWIFT_BUILD_SOURCEKIT OFF)
set(SWIFT_BUILD_STDLIB_EXTRA_TOOLCHAIN_CONTENT OFF)
set(SWIFT_BUILD_SYNTAXPARSERLIB OFF)
set(SWIFT_ENABLE_SOURCEKIT_TESTS OFF)
set(SWIFT_INCLUDE_DOCS OFF)
set(SWIFT_INCLUDE_TOOLS OFF)
set(SWIFT_INCLUDE_TESTS OFF)
set(SWIFT_INCLUDE_TEST_BINARIES OFF)
set(SWIFT_LIBRARY_EVOLUTION 0)
set(SWIFT_RUNTIME_OS_VERSIONING OFF)
set(SWIFT_HOST_VARIANT_ARCH ${SWIFT_TARGET_ARCH})
set(SWIFT_SDKS LINUX)
set(SWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_PATH ${STAGING_DIR_TARGET})
set(SWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_LIBC_INCLUDE_DIRECTORY ${STAGING_DIR_TARGET}/usr/include)
set(SWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_LIBC_ARCHITECTURE_INCLUDE_DIRECTORY ${STAGING_DIR_TARGET}/usr/include)
set(SWIFT_LINUX_${SWIFT_TARGET_ARCH}_ICU_I18N ${STAGING_DIR_TARGET}/usr/lib/libicui18n.so)
set(SWIFT_LINUX_${SWIFT_TARGET_ARCH}_ICU_UC ${STAGING_DIR_TARGET}/usr/lib/libicuuc.so)
set(SWIFT_PATH_TO_LIBDISPATCH_SOURCE ${UNPACKDIR}/libdispatch)
set(SWIFT_ENABLE_EXPERIMENTAL_CONCURRENCY ON)
set(SWIFT_ENABLE_EXPERIMENTAL_CXX_INTEROP ON)
set(SWIFT_ENABLE_EXPERIMENTAL_STRING_PROCESSING ON)
set(SWIFT_ENABLE_EXPERIMENTAL_DIFFERENTIABLE_PROGRAMMING ON)
set(SWIFT_ENABLE_EXPERIMENTAL_DISTRIBUTED ON)
set(SWIFT_ENABLE_EXPERIMENTAL_NONESCAPABLE_TYPES ON)
set(SWIFT_ENABLE_EXPERIMENTAL_OBSERVATION ON)
set(SWIFT_ENABLE_SYNCHRONIZATION ON)
set(SWIFT_PATH_TO_STRING_PROCESSING_SOURCE ${UNPACKDIR}/swift-experimental-string-processing)
set(SWIFT_SYNTAX_SOURCE_DIR ${UNPACKDIR}/swift-syntax)
set(SWIFTSYNTAX_SOURCE_DIR ${UNPACKDIR}/swift-syntax)
set(SWIFT_STANDARD_LIBRARY_SWIFT_FLAGS -Xcc --gcc-install-dir=${STAGING_DIR_TARGET}/usr/lib/gcc/${TARGET_SYS}/${SWIFT_GCC_VERSION} ${SWIFT_STDLIB_CXX_FLAGS} -no-verify-emitted-module-interface ${SWIFT_EXTRA_SWIFTC_CC_FLAGS})

set(ICU_I18N_LIBRARIES ${STAGING_DIR_TARGET}/usr/lib/libicui18n.so)
set(ICU_I18N_INCLUDE_DIRS ${STAGING_DIR_TARGET}/usr/include)
set(ICU_UC_LIBRARIES ${STAGING_DIR_TARGET}/usr/lib/libicuuc.so)
set(ICU_UC_INCLUDE_DIRS ${STAGING_DIR_TARGET}/usr/include)
set(LibRT_LIBRARIES ${STAGING_DIR_TARGET}/usr/lib/librt.a)
set(ZLIB_LIBRARY ${STAGING_DIR_TARGET}/usr/lib/libz.so)
EOF

    # pthreads does not work with armv7, so use c11 threading package in lieu
    if [ "${SWIFT_TARGET_ARCH}" = "armv7" ]; then
        echo "set(SWIFT_THREADING_PACKAGE c11)" >> ${SWIFT_CMAKE_TOOLCHAIN_FILE}
    fi

    cmake -S ${SWIFT_SRCDIR} -B ${SWIFT_BUILDDIR} -G Ninja \
        -DSDKROOT=${STAGING_DIR_TARGET} \
        -DCMAKE_SYSROOT=${STAGING_DIR_TARGET} \
        -DCMAKE_TOOLCHAIN_FILE=${SWIFT_CMAKE_TOOLCHAIN_FILE} \
        -DSWIFT_HOST_VARIANT_ARCH=${SWIFT_TARGET_ARCH} \
        -DSWIFT_NATIVE_CLANG_TOOLS_PATH=${SWIFT_NATIVE_PATH} \
        -DSWIFT_NATIVE_SWIFT_TOOLS_PATH=${SWIFT_NATIVE_PATH} \
        -DSWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_PATH=${STAGING_DIR_TARGET}  \
        -DSWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_LIBC_INCLUDE_DIRECTORY=${STAGING_DIR_TARGET}/usr/include  \
        -DSWIFT_SDK_LINUX_ARCH_${SWIFT_TARGET_ARCH}_LIBC_ARCHITECTURE_INCLUDE_DIRECTORY=${STAGING_DIR_TARGET}/usr/include \
        -DSWIFT_SDK_LINUX_CXX_OVERLAY_SWIFT_COMPILE_FLAGS="${SWIFT_CXX_OVERLAY_SWIFT_COMPILE_FLAGS}"
}

do_compile() {
    cd ${SWIFT_BUILDDIR} && ninja
}

do_install:prepend() {
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
    # install bridging and custom executor headers
    install -d ${D}${includedir}/swift
    install -m 0644 ${SWIFT_BUILDDIR}/include/swift/*.h ${D}${includedir}/swift
    install -m 0644 ${S}/lib/ClangImporter/SwiftBridging/swift/bridging* ${D}${includedir}/swift

    # install libraries
    install -d ${D}${libdir}
    cp -rfd ${SWIFT_BUILDDIR}/lib/swift ${D}${libdir}/
}

do_install:append() {
    # Remove CxxStdlib interface files that don't work in cross-compilation
    # The binary .swiftmodule files work fine without them
    rm -f ${D}${libdir}/swift/linux/CxxStdlib.swiftmodule/*/swiftinterface
    rm -f ${D}${libdir}/swift/linux/CxxStdlib.swiftmodule/*.swiftinterface
}

FILES:${PN} = "\
    ${libdir}/swift/linux/libswift_RegexParser.so \
    ${libdir}/swift/linux/libswiftSwiftPrivateThreadExtras.so \
    ${libdir}/swift/linux/libswift_Concurrency.so \
    ${libdir}/swift/linux/libswift_Differentiation.so \
    ${libdir}/swift/linux/libswiftDifferentiationUnittest.so \
    ${libdir}/swift/linux/libswiftDistributed.so \
    ${libdir}/swift/linux/libswiftRegexBuilder.so \
    ${libdir}/swift/linux/libswiftObservation.so \
    ${libdir}/swift/linux/libswiftSwiftOnoneSupport.so \
    ${libdir}/swift/linux/libswiftSwiftPrivateLibcExtras.so \
    ${libdir}/swift/linux/libswiftRuntimeUnittest.so \
    ${libdir}/swift/linux/libswift_StringProcessing.so \
    ${libdir}/swift/linux/libswiftGlibc.so \
    ${libdir}/swift/linux/libswiftCore.so \
    ${libdir}/swift/linux/libswift_Builtin_float.so \
    ${libdir}/swift/linux/libswiftSwiftPrivate.so \
    ${libdir}/swift/linux/libswiftSynchronization.so \
    ${libdir}/swift/linux/libswiftStdlibUnittest.so \
"

FILES:${PN}-dev = "\
    ${includedir}/swift \
    ${libdir}/swift/shims \
    ${libdir}/swift/apinotes \
    ${libdir}/swift/linux/libswiftCommandLineSupport.a \
    ${libdir}/swift/linux/libswiftCxxStdlib.a \
    ${libdir}/swift/linux/libswiftCxx.a \
    ${libdir}/swift/linux/libcxxshim.modulemap \
    ${libdir}/swift/linux/libstdcxx.modulemap \
    ${libdir}/swift/linux/libstdcxx.h \
    ${libdir}/swift/linux/libcxxshim.h \
    ${libdir}/swift/linux/libcxxstdlibshim.h \
    ${libdir}/swift/linux/${SWIFT_TARGET_ARCH} \
    ${libdir}/swift/linux/*.swiftmodule/* \
"

FILES:${PN}-staticdev = "\
    ${libdir}/swift_static \
"

FILES:${PN}-embedded = "\
    ${libdir}/swift/embedded \
"

INSANE_SKIP:${PN} = "file-rdeps buildpaths"
INSANE_SKIP:${PN}-dbg = "buildpaths"
INSANE_SKIP:${PN}-dev = "buildpaths staticdev"
INSANE_SKIP:${PN}-embedded = "buildpaths staticdev"

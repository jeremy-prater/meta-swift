inherit cmake

DEPENDS_append += " swift-native libgcc gcc glibc "

SWIFT_TARGET_ARCH = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7', 'aarch64', d)}"
SWIFT_TARGET_NAME = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7-unknown-linux-gnueabihf', 'aarch64-unknown-linux-gnueabi', d)}"
TARGET_CPU_NAME = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7-a', 'aarch64', d)}"

HOST_CC_ARCH_prepend = "-target ${SWIFT_TARGET_NAME}"

################################################################################
# NOTE: The host running bitbake must have lld available and the following     #
# must be added to the local.conf file:                                        #
#                                                                              #
# HOSTTOOLS += "ld.lld"                                                        #
#                                                                              #
################################################################################

# Use lld (see note above)
TARGET_LDFLAGS += "-fuse-ld=lld"

# Add build-id to generated binaries
TARGET_LDFLAGS += "-Xlinker --build-id=sha1"

# Use Apple's provided clang (it understands Apple's custom compiler flags)
# Made available via swift-native package.
OECMAKE_C_COMPILER = "clang"
OECMAKE_CXX_COMPILER = "clang++"

# Point clang to where the C++ runtime is for our target arch
RUNTIME_FLAGS = "-w -fuse-ld=lld -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/current"
TARGET_LDFLAGS += "-w -fuse-ld=lld -L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/current"

EXTRA_INCLUDE_FLAGS ?= ""
OECMAKE_C_FLAGS += "${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"
OECMAKE_CXX_FLAGS += "${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"
OECMAKE_ASM_FLAGS += "${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"

BUILD_MODE = "${@['release', 'debug'][d.getVar('DEBUG_BUILD') == '1']}"

# Additional parameters to pass to swiftc
EXTRA_SWIFTC_FLAGS ??= ""

SWIFT_FLAGS = "-target ${SWIFT_TARGET_NAME} -use-ld=lld \
-resource-dir ${STAGING_DIR_TARGET}/usr/lib/swift \
-module-cache-path ${B}/${BUILD_MODE}/ModuleCache \
-Xclang-linker -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/current \
-Xclang-linker -B${STAGING_DIR_TARGET}/usr/lib \
-Xcc -I${STAGING_DIR_NATIVE}/usr/lib/${TARGET_SYS}/gcc/${TARGET_SYS}/current/include \
-Xcc -I${STAGING_DIR_NATIVE}/usr/lib/${TARGET_SYS}/gcc/${TARGET_SYS}/current/include-fixed \
-L${STAGING_DIR_TARGET} \
-L${STAGING_DIR_TARGET}/lib \
-L${STAGING_DIR_TARGET}/usr/lib \
-L${STAGING_DIR_TARGET}/usr/lib/swift \
-L${STAGING_DIR_TARGET}/usr/lib/swift/linux \
-L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/current \
-sdk ${STAGING_DIR_TARGET} \
${EXTRA_SWIFTC_FLAGS} \
"

HOST_LLVM_PATH = "${STAGING_DIR_NATIVE}/opt/usr/lib/llvm-swift"
EXTRA_OECMAKE += " -DSWIFT_USE_LINKER=lld"
EXTRA_OECMAKE += " -DLLVM_USE_LINKER=lld"
EXTRA_OECMAKE += " -DLLVM_DIR=${HOST_LLVM_PATH}/lib/cmake/llvm"
EXTRA_OECMAKE += " -DLLVM_BUILD_LIBRARY_DIR=${HOST_LLVM_PATH}"

EXTRANATIVEPATH += "swift-tools"

################################################################################
# Create symlinks to the directories containing the gcc version specific       #
# headers, objects and libraries we need.                                      #
#                                                                              #
# We can't just use ${GCC_VERSION} in the path variables in the recipe         #
# because bitbake parses and expands variables before GCC_VERSION is           #
# defined. GCC_VERSION cannot be defined until the sysroot is populated        #
# because we inspect the sysroot to determine the GCC version number string.   #
# If there was an env or bitbake var with the GCC version, we could use that   #
# and avoid all of this but the closest thing we have access to is             #
# ${GCCVERSION} which yields and incomplete version number (ex: "9.%").        #
#                                                                              #
# Also there is some suspicion that these path variables and these symlinks    #
# may not be necessary if the --gcc-toolchain clang flag was used. But that    #
# is an unproven theory.                                                       #
################################################################################

do_create_gcc_version_symlinks() {
    GCC_VERSION=`basename ${STAGING_DIR_TARGET}/usr/include/c++/*`

    if [ ! -L "${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/current" ]; then
        cd ${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}
        ln -s -r ${GCC_VERSION} current
    fi

    if [ ! -L "${STAGING_DIR_TARGET}/usr/include/c++/current" ]; then
        cd ${STAGING_DIR_TARGET}/usr/include/c++
        ln -s -r ${GCC_VERSION} current
    fi

    if [ ! -L "${STAGING_DIR_NATIVE}/usr/lib/${TARGET_SYS}/gcc/${TARGET_SYS}/current" ]; then
        cd ${STAGING_DIR_NATIVE}/usr/lib/${TARGET_SYS}/gcc/${TARGET_SYS}
        ln -s -r ${GCC_VERSION} current
    fi
}

addtask do_create_gcc_version_symlinks after do_prepare_recipe_sysroot before do_configure


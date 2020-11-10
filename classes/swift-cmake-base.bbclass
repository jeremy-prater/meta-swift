
inherit cmake

DEPENDS += "swift-native"

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

# Point clang to where the C++ runtime is for our target arch
RUNTIME_FLAGS = "-B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/9.3.0"
TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/9.3.0"

EXTRA_INCLUDE_FLAGS ?= ""
OECMAKE_C_FLAGS += "${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"
OECMAKE_CXX_FLAGS += "${RUNTIME_FLAGS} ${EXTRA_INCLUDE_FLAGS}"

# Additional parameters to pass to swiftc
EXTRA_SWIFTC_FLAGS ??= ""

SWIFT_FLAGS = "-target armv7-unknown-linux-gnueabihf -use-ld=lld \
-resource-dir ${STAGING_DIR_TARGET}/usr/lib/swift \
-Xclang-linker -B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/9.3.0 \
-Xclang-linker -B${STAGING_DIR_TARGET}/usr/lib \
-Xcc -I${STAGING_DIR_NATIVE}/usr/lib/arm-poky-linux-gnueabi/gcc/arm-poky-linux-gnueabi/9.3.0/include \
-Xcc -I${STAGING_DIR_NATIVE}/usr/lib/arm-poky-linux-gnueabi/gcc/arm-poky-linux-gnueabi/9.3.0/include-fixed \
-L${STAGING_DIR_TARGET} \
-L${STAGING_DIR_TARGET}/lib \
-L${STAGING_DIR_TARGET}/usr/lib \
-L${STAGING_DIR_TARGET}/usr/lib \
-L${STAGING_DIR_TARGET}/usr/lib/swift \
-L${STAGING_DIR_TARGET}/usr/lib/swift/linux \
-L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/9.3.0 \
-sdk ${STAGING_DIR_TARGET} \
${EXTRA_SWIFTC_FLAGS} \
"

EXTRA_OECMAKE += '-DCMAKE_Swift_FLAGS="${SWIFT_FLAGS}"'

python do_get_gcc_version () {
    recipe_sysroot = d.getVar("STAGING_DIR_TARGET", True)
    cxx_include_base = recipe_sysroot + "/usr/include/c++"
    cxx_include_list = os.listdir(cxx_include_base)
    if len(cxx_include_list) != 1:
        bb.fatal("swift bbclass detected more than one c++ runtime, unable to determine which one to use")
    cxx_version = cxx_include_list[0]
    d.setVar('SWIFT_CXX_VERSION', cxx_version)
}

addtask do_get_gcc_version before do_configure after do_prepare_recipe_sysroot


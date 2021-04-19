
DEPENDS += "swift-native glibc gcc libgcc swift-stdlib libdispatch libfoundation"

# Default build directory for SPM is "./.build"
# (see 'swift [build|package|run|test] --help')
#
# We can allow for this to be changed by changing ${B} but one must be careful to also set
# "--build-path ${B}" for _ALL_ invocations of SPM within a recipe.
B ?= "${S}/.build"

# Additional parameters to pass to SPM
EXTRA_OESWIFT ?= ""

# Workaround complex macros that cannot be automatically imported by Swift.
# https://developer.apple.com/documentation/swift/imported_c_and_objective-c_apis/using_imported_c_macros_in_swift
#
# Seems that SO_RCVTIMEO and SO_SNDTIMEO definitions aren't working because they are expressions
# and not simple constants.
#
# This could be improved to replace just the specific lines that need fixing rather than rewriting
# the entire file.
def fix_socket_header(filename):
  with open(filename, 'r') as f:
    lines = f.readlines()

  os.remove(filename)

  with open(filename, 'w') as f:
    for line in lines:
      if line.startswith('#define SO_RCVTIMEO ') and ("SO_RCVTIMEO_OLD" in line) and ("?" in line):
        f.write('#define SO_RCVTIMEO    SO_RCVTIMEO_OLD\n')
      elif line.startswith('#define SO_SNDTIMEO ') and ("SO_SNDTIMEO_OLD" in line) and ("?" in line):
        f.write('#define SO_SNDTIMEO    SO_SNDTIMEO_OLD\n')
      else:
        f.write(line)


python swift_do_configure() {
    import os
    import os.path
    import shutil

    workdir = d.getVar("WORKDIR", True)
    recipe_sysroot = d.getVar("STAGING_DIR_TARGET", True)

    # Workaround complex macros that cannot be automatically imported by Swift.
    # https://developer.apple.com/documentation/swift/imported_c_and_objective-c_apis/using_imported_c_macros_in_swift
    #
    # Seems that SO_RCVTIMEO and SO_SNDTIMEO definitions aren't working because they are expressions
    # and not simple constants.
    socket_header = recipe_sysroot + "/usr/include/asm-generic/socket.h"
    fix_socket_header(socket_header)

    # Detect the version of the C++ runtime
    # This is used to determine necessary include paths
    cxx_include_base = recipe_sysroot + "/usr/include/c++"
    cxx_include_list = os.listdir(cxx_include_base)
    if len(cxx_include_list) != 1:
        bb.fatal("swift bbclass detected more than one c++ runtime, unable to determine which one to use")
    cxx_version = cxx_include_list[0]

    d.setVar('SWIFT_CXX_VERSION', cxx_version)

    swift_destination_template = """{
        "version":1,
        "sdk":"${STAGING_DIR_TARGET}/",
        "toolchain-bin-dir":"${STAGING_DIR_NATIVE}/opt/usr/bin",
        "target":"armv7-unknown-linux-gnueabihf",
        "dynamic-library-extension":"so",
        "extra-cc-flags":[
            "-fPIC",
            "-I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_CXX_VERSION}",
            "-I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_CXX_VERSION}/${TARGET_SYS}",
            "-I${STAGING_DIR_NATIVE}/opt/usr/lib/clang/10.0.0/include",
            "-I${STAGING_DIR_NATIVE}/opt/usr/lib/clang/10.0.0/include-fixed"
        ],
        "extra-swiftc-flags":[
            "-target",
            "armv7-unknown-linux-gnueabihf",
            "-use-ld=lld",
            "-tools-directory",
            "/usr/bin",

            "-Xlinker", "-rpath", "-Xlinker", "/usr/lib/swift/linux",

            "-Xlinker",
            "-L${STAGING_DIR_TARGET}",

            "-Xlinker",
            "-L${STAGING_DIR_TARGET}/lib",

            "-Xlinker",
            "-L${STAGING_DIR_TARGET}/usr/lib",

            "-Xlinker",
            "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux",

            "-Xlinker",
            "-L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_CXX_VERSION}",

            "-Xlinker",
            "--build-id=sha1",

            "-I${STAGING_INCDIR}",
            "-I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_CXX_VERSION}",
            "-I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_CXX_VERSION}/${TARGET_SYS}",
            "-I${STAGING_DIR_NATIVE}/opt/usr/lib/clang/10.0.0/include",
            "-I${STAGING_DIR_NATIVE}/opt/usr/lib/clang/10.0.0/include-fixed",

            "-resource-dir", "${STAGING_DIR_TARGET}/usr/lib/swift",
            "-module-cache-path", "${B}/release/ModuleCache",
            "-Xclang-linker", "-B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_CXX_VERSION}",
            "-Xclang-linker", "-B${STAGING_DIR_TARGET}/usr/lib",

            "-sdk", "${STAGING_DIR_TARGET}"
        ],
        "extra-cpp-flags":[
            "-lstdc++"
        ]
    }"""

    swift_destination =  d.expand(swift_destination_template)

    d.delVar("SWIFT_CXX_VERSION")

    configJSON = open(workdir + "/destination.json", "w")
    configJSON.write(swift_destination)
    configJSON.close()
}

swift_do_compile()  {
    cd ${S}

    swift build --build-path ${B} -v -c release --destination ${WORKDIR}/destination.json ${EXTRA_OESWIFT}
}

EXPORT_FUNCTIONS do_configure do_compile

EXTRANATIVEPATH += "swift-tools"

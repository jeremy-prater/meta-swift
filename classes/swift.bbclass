# avoid conflicts with meta-clang
TOOLCHAIN = "gcc"

SWIFT_BUILD_TESTS ?= "${DEBUG_BUILD}"

DEPENDS += "swift-native glibc gcc libgcc swift-stdlib libdispatch swift-foundation"
DEPENDS += "${@oe.utils.conditional('SWIFT_BUILD_TESTS', '1', 'swift-xctest swift-testing', '', d)}"

# Default build directory for SPM is "./.build"
# (see 'swift [build|package|run|test] --help')
#
# We can allow for this to be changed by changing ${B} but one must be careful to also set
# "--build-path ${B}" for _ALL_ invocations of SPM within a recipe.
B ?= "${S}/.build"
EXTERNALSRC_BUILD ?= "${EXTERNALSRC}/.build"

BUILD_MODE = "${@['release', 'debug'][d.getVar('DEBUG_BUILD') == '1']}"
BUILD_DIR = "${B}/${BUILD_MODE}"

# Additional parameters to pass to SPM
EXTRA_OESWIFT ?= ""

SWIFT_TARGET_NAME = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7-unknown-linux-gnueabihf', 'aarch64-unknown-linux-gnu', d)}"
SWIFT_TARGET_ARCH = "${@oe.utils.conditional('TARGET_ARCH', 'arm', 'armv7', 'aarch64', d)}"

do_fix_gcc_install_dir() {
    # symbolic links do not work, will not be found by Swift clang driver
    # this is necessary to make the libstdc++ location heuristic work, necessary for C++ interop
    (cd ${STAGING_DIR_TARGET}/usr/lib && rm -rf gcc && mkdir -p gcc && cp -rp ${SWIFT_TARGET_ARCH}-oe-linux gcc)
}

addtask fix_gcc_install_dir before do_configure after do_prepare_recipe_sysroot

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

# Support for SwiftPM fetching packages and their GitHub submodules
do_swift_package_resolve[depends] += "unzip-native:do_populate_sysroot swift-native:do_populate_sysroot"
do_swift_package_resolve[network] = "1"
do_swift_package_resolve[vardepsexclude] = "BB_ORIGENV"

python do_swift_package_resolve() {
    import subprocess
    import os

    s = d.getVar('S')
    b = d.getVar('B')

    ssh_auth_sock = d.getVar('BB_ORIGENV')['SSH_AUTH_SOCK']

    env = os.environ.copy()
    env['SSH_AUTH_SOCK'] = ssh_auth_sock

    ret = subprocess.call(['swift', 'package', 'resolve', '--package-path', s, '--build-path', b], env=env)
    if ret != 0:
        bb.fatal('swift package resolve failed')

    # note: --depth 1 requires git version 2.43.0 or later
    for package in os.listdir(path=f'{b}/checkouts'):
        package_dir = f'{b}/checkouts/{package}'
        ret = subprocess.call(['git', 'submodule', 'update', '--init', '--recursive', '--depth', '1'], cwd=package_dir, env=env)
        if ret != 0:
            bb.fatal('git submodule update failed')
}

addtask swift_package_resolve after do_unpack before do_compile

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
    if 'current' in cxx_include_list:
        cxx_include_list.remove('current')
    if len(cxx_include_list) != 1:
        bb.fatal("swift bbclass detected more than one c++ runtime, unable to determine which one to use")
    cxx_version = cxx_include_list[0]

    d.setVar('SWIFT_CXX_VERSION', cxx_version)

    swift_destination_template = """{
        "version":1,
        "sdk":"${STAGING_DIR_TARGET}/",
        "toolchain-bin-dir":"${STAGING_DIR_NATIVE}/usr/bin",
        "target":"${SWIFT_TARGET_NAME}",
        "dynamic-library-extension":"so",
        "extra-cc-flags":[
            "-fPIC",
            "-I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_CXX_VERSION}",
            "-I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_CXX_VERSION}/${TARGET_SYS}",
            "-I${STAGING_DIR_NATIVE}/usr/lib/clang/17/include",
            "-I${STAGING_DIR_NATIVE}/usr/lib/clang/17/include-fixed"
        ],
        "extra-swiftc-flags":[
            "-target",
            "${SWIFT_TARGET_NAME}",
            "-use-ld=lld",
            "-tools-directory",
            "${STAGING_DIR_NATIVE}/usr/bin",

            "-enforce-exclusivity=unchecked",
            "-enforce-exclusivity=none",

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
            "-I${STAGING_DIR_NATIVE}/usr/lib/clang/17/include",
            "-I${STAGING_DIR_NATIVE}/usr/lib/clang/17/include-fixed",

            "-resource-dir", "${STAGING_DIR_TARGET}/usr/lib/swift",
            "-module-cache-path", "${B}/${BUILD_MODE}/ModuleCache",
            "-Xclang-linker", "-B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_CXX_VERSION}",
            "-Xclang-linker", "-B${STAGING_DIR_TARGET}/usr/lib",

            "-Xcc", "--gcc-install-dir=${STAGING_DIR_TARGET}/usr/lib/gcc/${TARGET_SYS}/${SWIFT_CXX_VERSION}",

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

# ideally this should be handled by do_swift_package_resolve but doesn't always appear to be the case
do_compile[network] = "1"
swift_do_compile[vardepsexclude] = "BB_ORIGENV"

python swift_do_compile() {
    import subprocess
    import os
    import shlex

    s = d.getVar('S')
    b = d.getVar('B')
    build_mode = d.getVar('BUILD_MODE')
    workdir = d.getVar("WORKDIR", True)
    destination_json = workdir + '/destination.json'
    extra_oeswift = shlex.split(d.getVar('EXTRA_OESWIFT'))
    ssh_auth_sock = d.getVar('BB_ORIGENV')['SSH_AUTH_SOCK']
    recipe_sysroot = d.getVar("STAGING_DIR_TARGET", True)

    env = os.environ.copy()
    env['SSH_AUTH_SOCK'] = ssh_auth_sock
    env['SYSROOT'] = recipe_sysroot

    args = ['swift', 'build', '--package-path', s, '--build-path', b, '-c', build_mode, '--destination', destination_json] + extra_oeswift

    ret = subprocess.call(args, env=env, cwd=s)
    if ret != 0:
        bb.fatal('swift build failed')

    if d.getVar('SWIFT_BUILD_TESTS') == '1':
        if d.getVar('DEBUG_BUILD') != '1':
            bb.warn('building Swift tests with release build, @testable imports may fail')

        # FIXME: why do we need to specify -lXCTest and -lTesting explicitly
        test_args = ['--build-tests', '-Xlinker', '-lXCTest', '-Xlinker', '-lTesting']
        ret = subprocess.call(args + test_args + extra_oeswift, env=env, cwd=s)
        if ret != 0:
            bb.fatal('swift build --build-tests failed')
}

EXPORT_FUNCTIONS do_configure do_compile

EXTRANATIVEPATH += "swift-tools"

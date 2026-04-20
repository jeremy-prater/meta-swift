inherit swift-common

SWIFT_BUILD_TESTS ?= "${DEBUG_BUILD}"

DEPENDS:append = " swift-stdlib libdispatch swift-foundation"
DEPENDS:append = " ${@oe.utils.conditional('SWIFT_BUILD_TESTS', '1', ' swift-xctest swift-testing', '', d)}"

# Depending on the Yocto version, the sources may be in ${UNPACKDIR} or may just need to be placed
# at ${WORKDIR}/git instead.
SWIFT_UNPACKDIR = "${@oe.utils.conditional('UNPACKDIR', '${WORKDIR}', '${WORKDIR}/git', '${UNPACKDIR}', d)}"

# Default build directory for SPM is "./.build"
# (see 'swift [build|package|run|test] --help')
#
# We can allow for this to be changed by changing ${B} but one must be careful to also set
# "--build-path ${B}" for _ALL_ invocations of SPM within a recipe.
B ?= "${S}/.build"
EXTERNALSRC_BUILD ?= "${EXTERNALSRC}/.build"

BUILD_DIR = "${B}/${BUILD_MODE}"

# Additional parameters to pass to SPM
EXTRA_OESWIFT ?= ""

do_fix_gcc_install_dir() {
    # symbolic links do not work, will not be found by Swift clang driver
    # this is necessary to make the libstdc++ location heuristic work, necessary for C++ interop
    (cd ${STAGING_DIR_TARGET}/usr/lib && rm -rf gcc && mkdir -p gcc && cp -rp ${TARGET_ARCH}${TARGET_VENDOR}-${TARGET_OS} gcc)
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
    recipe_sysroot_native = d.getVar("STAGING_DIR_NATIVE", True)

    env = os.environ.copy()

    ssh_auth_sock = d.getVar('BB_ORIGENV').get('SSH_AUTH_SOCK')
    if ssh_auth_sock:
        env['SSH_AUTH_SOCK'] = ssh_auth_sock

    ret = subprocess.call([f'{recipe_sysroot_native}/usr/bin/swift', 'package', 'resolve', '--package-path', s, '--build-path', b], env=env)
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

SWIFT_SDK_ID ?= "wrynose-${SWIFT_TARGET_ARCH}"
SWIFT_SDK_BUNDLE_DIR = "${WORKDIR}/swift-sdks"

python swift_do_configure() {
    import json
    import os
    import shlex

    workdir = d.getVar("WORKDIR", True)
    recipe_sysroot = d.getVar("STAGING_DIR_TARGET", True)

    # Workaround complex macros that cannot be automatically imported by Swift.
    # https://developer.apple.com/documentation/swift/imported_c_and_objective-c_apis/using_imported_c_macros_in_swift
    #
    # Seems that SO_RCVTIMEO and SO_SNDTIMEO definitions aren't working because they are expressions
    # and not simple constants.
    socket_header = recipe_sysroot + "/usr/include/asm-generic/socket.h"
    fix_socket_header(socket_header)

    target_cc_arch = shlex.split(d.getVar("TARGET_CC_ARCH"))
    target_triple = d.getVar("SWIFT_TARGET_NAME")
    target_sys = d.getVar("TARGET_SYS")
    staging_incdir = d.getVar("STAGING_INCDIR")
    staging_dir_native = d.getVar("STAGING_DIR_NATIVE")
    staging_dir_target = d.getVar("STAGING_DIR_TARGET")
    swift_clang_version = d.getVar("SWIFT_CLANG_VERSION")
    swift_gcc_version = d.getVar("SWIFT_GCC_VERSION")
    build_dir = d.getVar("B")
    build_mode = d.getVar("BUILD_MODE")
    sdk_id = d.getVar("SWIFT_SDK_ID")
    bundle_parent = d.getVar("SWIFT_SDK_BUNDLE_DIR")

    gcc_cxx_include = staging_dir_target + "/usr/include/c++/" + swift_gcc_version
    gcc_cxx_include_target = gcc_cxx_include + "/" + target_sys
    clang_resource_include = staging_dir_native + "/usr/lib/clang/" + swift_clang_version + "/include"
    clang_resource_include_fixed = staging_dir_native + "/usr/lib/clang/" + swift_clang_version + "/include-fixed"

    # Include paths safe on the C command line. GCC's libstdc++ ships a C++-only
    # stdatomic.h shim that expands to nothing in C mode; clang's stdatomic.h
    # #include_nexts into it, so adding the C++ header dirs to C flags breaks
    # C11 atomics. Keep those paths on cxxCompiler only.
    c_include_flags = [
        "-I" + staging_incdir,
        "-I" + clang_resource_include,
        "-I" + clang_resource_include_fixed,
    ]
    cxx_only_include_flags = [
        "-I" + gcc_cxx_include,
        "-I" + gcc_cxx_include_target,
    ]

    c_cli_options = target_cc_arch + ["-fPIC"] + c_include_flags
    cxx_cli_options = (
        target_cc_arch
        + ["-fPIC", "-Wno-invalid-constexpr"]
        + c_include_flags
        + cxx_only_include_flags
        + ["-lstdc++"]
    )

    swiftc_cli_options = [
        "-target", target_triple,
        "-use-ld=lld",
        "-tools-directory", staging_dir_native + "/usr/bin",

        "-enforce-exclusivity=unchecked",
        "-resource-dir", staging_dir_target + "/usr/lib/swift",
        "-module-cache-path", build_dir + "/" + build_mode + "/ModuleCache",
        "-sdk", staging_dir_target,

        # Suppress swiftc's automatic -rpath <resource-dir>/linux injection,
        # which would otherwise bake the sysroot path into RUNPATH. The
        # target-side rpath is added explicitly below.
        "-no-toolchain-stdlib-rpath",

        "-I" + staging_incdir,
        "-I" + gcc_cxx_include,
        "-I" + gcc_cxx_include_target,
        "-I" + clang_resource_include,
        "-I" + clang_resource_include_fixed,

        "-Xlinker", "-rpath", "-Xlinker", "/usr/lib/swift/linux",

        "-Xlinker", "-L" + staging_dir_target,
        "-Xlinker", "-L" + staging_dir_target + "/lib",
        "-Xlinker", "-L" + staging_dir_target + "/usr/lib",
        "-Xlinker", "-L" + staging_dir_target + "/usr/lib/swift/linux",
        "-Xlinker", "-L" + staging_dir_target + "/usr/lib/" + target_sys + "/" + swift_gcc_version,

        "-Xlinker", "--build-id=sha1",

        "-Xclang-linker", "-B" + staging_dir_target + "/usr/lib",
        "-Xclang-linker", "-B" + staging_dir_target + "/usr/lib/" + target_sys + "/" + swift_gcc_version,

        "-Xcc", "--gcc-install-dir=" + staging_dir_target + "/usr/lib/gcc/" + target_sys + "/" + swift_gcc_version,
    ]
    for flag in target_cc_arch:
        swiftc_cli_options += ["-Xcc", flag]

    # v4 Swift SDK artifact bundle layout:
    #   <bundle_parent>/<sdk_id>.artifactbundle/
    #     info.json                          (ArtifactsArchiveMetadata, schema 1.0)
    #     <sdk_id>/
    #       swift-sdk.json                   (SwiftSDKMetadataV4, schema 4.0)
    #       toolset.json                     (Toolset, schema 1.0)
    # Passed to swift build via --swift-sdks-path <bundle_parent> --swift-sdk <sdk_id>.
    bundle_root = os.path.join(bundle_parent, sdk_id + ".artifactbundle")
    artifact_dir = os.path.join(bundle_root, sdk_id)
    os.makedirs(artifact_dir, exist_ok=True)

    info_json = {
        "schemaVersion": "1.0",
        "artifacts": {
            sdk_id: {
                "type": "swiftSDK",
                "version": "0.0.1",
                "variants": [
                    {"path": sdk_id},
                ],
            },
        },
    }

    toolset_json = {
        "schemaVersion": "1.0",
        "swiftCompiler": {"extraCLIOptions": swiftc_cli_options},
        "cCompiler": {"extraCLIOptions": c_cli_options},
        "cxxCompiler": {"extraCLIOptions": cxx_cli_options},
    }

    swift_sdk_json = {
        "schemaVersion": "4.0",
        "targetTriples": {
            target_triple: {
                "sdkRootPath": staging_dir_target,
                "toolsetPaths": ["toolset.json"],
            },
        },
    }

    with open(os.path.join(bundle_root, "info.json"), "w") as f:
        json.dump(info_json, f, indent=2)
    with open(os.path.join(artifact_dir, "toolset.json"), "w") as f:
        json.dump(toolset_json, f, indent=2)
    with open(os.path.join(artifact_dir, "swift-sdk.json"), "w") as f:
        json.dump(swift_sdk_json, f, indent=2)
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
    sdk_id = d.getVar('SWIFT_SDK_ID')
    sdks_path = d.getVar('SWIFT_SDK_BUNDLE_DIR')
    extra_oeswift = shlex.split(d.getVar('EXTRA_OESWIFT'))
    ssh_auth_sock = d.getVar('BB_ORIGENV').get('SSH_AUTH_SOCK')
    recipe_sysroot = d.getVar("STAGING_DIR_TARGET", True)
    recipe_sysroot_native = d.getVar("STAGING_DIR_NATIVE", True)

    env = os.environ.copy()
    if ssh_auth_sock:
        env['SSH_AUTH_SOCK'] = ssh_auth_sock
    env['SYSROOT'] = recipe_sysroot

    args = [f'{recipe_sysroot_native}/usr/bin/swift', 'build', '--package-path', s, '--build-path', b, '-c', build_mode, '--swift-sdks-path', sdks_path, '--swift-sdk', sdk_id] + extra_oeswift

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

do_package_update() {
    cd ${S}
    swift package update

    # Iterate over the search dirs for this recipes' files
    # The first one that has a Package.resolved is the one bitbake got the file
    # from in the first places
    RESOLVED_PATH=""
    for i in $(echo "${FILESPATH}" | tr ':' '\n'); do
        if [ -r "${i}"/Package.resolved ]; then
            RESOLVED_PATH="${i}/Package.resolved"
            cp Package.resolved "${RESOLVED_PATH}"
            bbwarn "Replaced ${RESOLVED_PATH} with updated Package.resolved."
            break
        fi
    done
    [ -z "${RESOLVED_PATH}" ] && bbwarn "Updated Package.resolved located at ${S}/Package.resolved" || :
}
do_package_update[network] = "1"
addtask do_package_update after do_configure

EXPORT_FUNCTIONS do_configure do_compile do_package_update

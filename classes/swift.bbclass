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
    recipe_sysroot_native_lib = d.getVar("STAGING_LIBDIR_NATIVE", True)

    env = os.environ.copy()

    # Prefer curl-native over the host's libcurl: the prebuilt swift-native's
    # libFoundationNetworking (e.g. from amazonlinux2) is linked against a
    # specific libcurl.so.4 and aborts loading an incompatible host libcurl
    # (issue #42).
    ld_path = recipe_sysroot_native_lib
    if env.get('LD_LIBRARY_PATH'):
        ld_path += ':' + env['LD_LIBRARY_PATH']
    env['LD_LIBRARY_PATH'] = ld_path

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
    target_arch = d.getVar("TARGET_ARCH")
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

    use_libcxx = bb.utils.contains("SWIFT_CXX_RUNTIME", "llvm", True, False, d)
    clang_resource_include = staging_dir_native + "/usr/lib/clang/" + swift_clang_version + "/include"
    clang_resource_include_fixed = staging_dir_native + "/usr/lib/clang/" + swift_clang_version + "/include-fixed"

    # Remap build-host paths out of debug info, __FILE__, and coverage records
    # so the resulting binaries don't embed strings like
    # /home/.../build/tmp/work/<pn>/<pv>/sources/... -- those trip OE's
    # `buildpaths` QA and are useless to anyone debugging on-device. Mirrors
    # the standard DEBUG_PREFIX_MAP convention oe-core uses for non-Swift
    # recipes (see base.bbclass); swift-cmake-base.bbclass clears the stock
    # DEBUG_PREFIX_MAP for reasons unrelated to SwiftPM, so we thread the
    # equivalent through the toolset.json ourselves.
    #   workdir      -> /usr/src/debug/<pn>/<pv>-<pr>
    #   sysroot host -> "" (remove entirely; sysroot paths are build artifacts)
    #   sysroot nat  -> ""
    extendpe = d.getVar("EXTENDPE") or ""
    debug_src_dir = "/usr/src/debug/" + d.getVar("PN") + "/" + extendpe + d.getVar("PV") + "-" + d.getVar("PR")
    prefix_maps = [
        (workdir, debug_src_dir),
        (staging_dir_target, ""),
        (staging_dir_native, ""),
    ]
    clang_prefix_map_flags = ["-ffile-prefix-map=" + src + "=" + dst for src, dst in prefix_maps]
    # Swift does not accept the umbrella -file-prefix-map, and its
    # -debug-prefix-map wants the flag and value as two tokens (the
    # -ffile-prefix-map=K=V single-token form works for Clang but not swiftc).
    # Add the -ffile-prefix-map variant via -Xcc further down so the Clang
    # importer remaps too.
    swift_prefix_map_flags = []
    for src, dst in prefix_maps:
        swift_prefix_map_flags += ["-debug-prefix-map", src + "=" + dst]

    # Include paths safe on the C command line. GCC's libstdc++ ships a C++-only
    # stdatomic.h shim that expands to nothing in C mode; clang's stdatomic.h
    # #include_nexts into it, so adding the C++ header dirs to C flags breaks
    # C11 atomics. Keep those paths on cxxCompiler only.
    c_include_flags = [
        "-I" + staging_incdir,
        "-I" + clang_resource_include,
        "-I" + clang_resource_include_fixed,
    ]
    if use_libcxx:
        # SWIFT_CXX_RUNTIME=llvm: point Clang at the system libc++ (OE-core's libcxx
        # recipe) via -stdlib=libc++ and the c++/v1 header dir; -stdlib also makes
        # the driver auto-link -lc++ -lc++abi, so no explicit -l is needed.
        cxx_only_include_flags = [
            "-stdlib=libc++",
            "-I" + staging_dir_target + "/usr/include/c++/v1",
        ]
        cxx_link_flags = []
    else:
        gcc_cxx_include = staging_dir_target + "/usr/include/c++/" + swift_gcc_version
        gcc_cxx_include_target = gcc_cxx_include + "/" + target_sys
        cxx_only_include_flags = [
            "-I" + gcc_cxx_include,
            "-I" + gcc_cxx_include_target,
        ]
        cxx_link_flags = ["-lstdc++"]

    c_cli_options = target_cc_arch + ["-fPIC"] + c_include_flags
    if use_libcxx:
        # Under libc++, cxx_only_include_flags (-stdlib=libc++ and /c++/v1) must
        # precede c_include_flags so libc++'s own wrappers (c++/v1/{cstddef,cfloat,...})
        # win over the matching C headers. Otherwise libc++'s <cstddef>/<cfloat>
        # abort with "didn't find libc++'s <X> header".
        ordered_cxx_include_flags = cxx_only_include_flags + c_include_flags
    else:
        # Under libstdc++, keep c_include_flags first: GCC's C++ header dir ships
        # its own <math.h>/<complex.h> wrappers that (via <cmath>) transitively
        # build libstdcxx.modulemap's `std` module and fail on glibc >= 2.38 --
        # the same path that the libstdc++-trap patches work around. Putting
        # cxx_only_include_flags first would undo those fixes.
        ordered_cxx_include_flags = c_include_flags + cxx_only_include_flags
    cxx_cli_options = (
        target_cc_arch
        + ["-fPIC", "-Wno-invalid-constexpr"]
        + ordered_cxx_include_flags
        + cxx_link_flags
    )

    c_cli_options += clang_prefix_map_flags
    cxx_cli_options += clang_prefix_map_flags

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

        # Keep libstdc++ C++ headers off swiftc's bare -I, which would otherwise
        # put them on Clang's header search when the Swift Clang importer
        # parses a C module -- causing <math.h> to resolve to the libstdc++
        # C++ wrapper and fail C-mode parsing with "redefinition of 'cosh'".
        # C++ interop, if ever enabled, can add these back via EXTRA_OESWIFT
        # (or cxxCompiler in the toolset).
        "-I" + staging_incdir,
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
    if use_libcxx:
        # -Xcc -stdlib=libc++: Swift's Clang importer parses C++ with libc++,
        # so `import CxxStdlib` resolves via libcxxshim.modulemap rather than
        # libstdcxx.modulemap.
        # -Xclang-linker -stdlib=libc++: the Clang driver Swift invokes as the
        # linker auto-links libc++/libc++abi instead of libstdc++.
        swiftc_cli_options += [
            "-Xcc", "-stdlib=libc++",
            "-Xclang-linker", "-stdlib=libc++",
        ]
    for flag in target_cc_arch:
        swiftc_cli_options += ["-Xcc", flag]

    # x86_64 + libstdc++: -march=x86-64-v3 (and anything implying SSE3)
    # re-triggers the std <-> _Builtin_intrinsics cycle in downstream Swift
    # compiles that import SwiftGlibc -- complex.h pulls in <complex> which
    # pulls in <random> which pulls in opt_random.h -> <pmmintrin.h> ->
    # _Builtin_intrinsics -> mm_malloc.h -> stdlib.h -> cstdlib. Undefine
    # __SSE3__ for all Clang-importer parses on x86_64+gnu, matching the
    # sidestep applied to swift-stdlib's own CxxStdlib overlay build. libc++
    # consumers use libcxxshim and don't hit this path.
    if target_arch == "x86_64" and not use_libcxx:
        swiftc_cli_options += ["-Xcc", "-mno-sse3"]

    # Swift frontend handles -file-prefix-map directly (Swift 5.7+); also feed
    # the equivalent -ffile-prefix-map to Clang via -Xcc so the Clang importer
    # remaps C/ObjC/C++ headers pulled in for interop.
    swiftc_cli_options += swift_prefix_map_flags
    for flag in clang_prefix_map_flags:
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
    recipe_sysroot_native_lib = d.getVar("STAGING_LIBDIR_NATIVE", True)

    env = os.environ.copy()

    # Prefer curl-native over the host's libcurl: the prebuilt swift-native's
    # libFoundationNetworking (e.g. from amazonlinux2) is linked against a
    # specific libcurl.so.4 and aborts loading an incompatible host libcurl
    # (issue #42).
    ld_path = recipe_sysroot_native_lib
    if env.get('LD_LIBRARY_PATH'):
        ld_path += ':' + env['LD_LIBRARY_PATH']
    env['LD_LIBRARY_PATH'] = ld_path

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

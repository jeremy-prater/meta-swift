SUMMARY = "Swift stage-0 bootstrap compiler, built from source with no prebuilt Swift"
DESCRIPTION = "Minimal Swift compiler + stdlib + libdispatch built from upstream \
sources using only a C/C++ host toolchain. Pinned to ${SWIFT_BOOTSTRAP_VERSION}, \
independently of SWIFT_VERSION, because 6.3.x is the last series that can be \
built without an existing Swift compiler. Staged under the same private prefix \
as swift-bootstrap-native so swift-native can consume either as its bootstrap. \
Alternative provider for virtual/swift-bootstrap-native; select with: \
PREFERRED_PROVIDER_virtual/swift-bootstrap-native = \"swift-stage0-native\""
HOMEPAGE = "https://swift.org/install/"

LICENSE = "Apache-2.0"

# The bootstrap is pinned to ITS OWN version, not SWIFT_VERSION: 6.3.x is the last
# series that can be built without an existing Swift compiler. See
# swift-bootstrap-version.inc.
require swift-bootstrap-version.inc
require swift-bootstrap-source.inc
require swift-bootstrap-paths.inc
PV = "${SWIFT_BOOTSTRAP_VERSION}+git"

FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI = "\
    file://yocto-stage0.preset.ini \
${SWIFT_SRC_URI}"

SWIFT_PRESET_FILE = "${UNPACKDIR}/yocto-stage0.preset.ini"

LIC_FILES_CHKSUM = "file://${UNPACKDIR}/swift/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

S = "${UNPACKDIR}/swift"

PROVIDES += "virtual/swift-bootstrap-native"

# The whole point of this recipe: no swift-bootstrap-native, and no Swift
# binary anywhere in DEPENDS. clang-native and lld-native both come from
# OE-core (meta/recipes-devtools/clang, BBCLASSEXTEND = "native"), so unlike
# PR #59 this needs neither meta-clang nor a dynamic-layer clang bbappend --
# and nothing has to go looking for ld.lld inside another recipe's WORKDIR.
DEPENDS = "clang-native lld-native python3-native cmake-native ninja-native \
           pkgconfig-native libxml2-native sqlite3-native curl-native \
           ncurses-native rsync-native"

inherit native


do_configure[network] = "0"
do_compile[network] = "0"

do_compile() {
    # build-script's toolchain discovery searches PATH for clang/clang++ and
    # sets cc = cxx = None if it finds neither -- there is no gcc fallback.
    # host_cc/host_cxx below are preset SUBSTITUTIONS, not command-line flags:
    # --host-cc/--host-cxx are rejected in preset mode. See the comment in
    # yocto-stage0.preset.ini.
    export CC="${STAGING_BINDIR_NATIVE}/clang"
    export CXX="${STAGING_BINDIR_NATIVE}/clang++"

    # OE's native LDFLAGS (-Wl,--enable-new-dtags etc.) reach swiftc verbatim
    # through cmake, and swiftc rejects bare -Wl, flags. build-script manages
    # its own link options. Same reason as swift-native.bb.
    unset LDFLAGS
    unset BUILD_LDFLAGS

    # Key the incremental tree on the identity of the compiler that built it
    # (clang-native can be upgraded without changing this task's staged path)
    # plus the install destdir; wipe only on mismatch. Same rationale as
    # swift-native.bb's do_compile.
    bootstrap_id="$(${STAGING_BINDIR_NATIVE}/clang --version 2>&1 | head -1; echo destdir=${WORKDIR}/dest)"
    stamp=${UNPACKDIR}/build/.oe-bootstrap-id
    if [ ! -f "$stamp" ] || [ "$(cat "$stamp")" != "$bootstrap_id" ]; then
        rm -rf ${UNPACKDIR}/build
        mkdir -p ${UNPACKDIR}/build
        printf '%s' "$bootstrap_id" > "$stamp"
    fi
    # build-script's install step fully regenerates the dest tree on every
    # run; wiping it here prevents files removed from the install set ever
    # lingering from a previous configuration.
    rm -rf ${WORKDIR}/dest

    cd ${S}
    ./utils/build-script \
        --preset-file=${SWIFT_PRESET_FILE} \
        --preset=yocto_stage0 \
        host_cc=${STAGING_BINDIR_NATIVE}/clang \
        host_cxx=${STAGING_BINDIR_NATIVE}/clang++ \
        install_destdir=${WORKDIR}/dest \
        -j ${@oe.utils.parallel_make(d) or 1}
}

do_install() {
    # See swift-native.bb's do_install: build-script installed into
    # ${WORKDIR}/dest, ${D} is freshly wiped by default cleandirs, hardlink-
    # copy the tree under the shared bootstrap prefix.
    [ -d ${WORKDIR}/dest/usr ] || bbfatal "no build-script install tree at ${WORKDIR}/dest/usr"
    install -d ${D}${SWIFT_BOOTSTRAP_DIR}
    cp -al ${WORKDIR}/dest/usr ${D}${SWIFT_BOOTSTRAP_DIR}/usr
}

PACKAGES = "${PN}"
FILES:${PN} = "${SWIFT_BOOTSTRAP_DIR}"
SYSROOT_DIRS:append = " ${SWIFT_BOOTSTRAP_DIR}"

INSANE_SKIP:${PN} = "already-stripped arch buildpaths dev-so file-rdeps libdir staticdev"

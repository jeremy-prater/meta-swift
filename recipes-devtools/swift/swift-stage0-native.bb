SUMMARY = "Swift stage-0 bootstrap compiler, built from source with no prebuilt Swift"
DESCRIPTION = "Minimal Swift compiler + stdlib + libdispatch built from upstream \
sources using only a C/C++ host toolchain. Staged under the same private prefix \
as swift-bootstrap-native so swift-native can consume either as its bootstrap. \
Alternative provider for virtual/swift-bootstrap-native; select with: \
PREFERRED_PROVIDER_virtual/swift-bootstrap-native = \"swift-stage0-native\""
HOMEPAGE = "https://swift.org/install/"

LICENSE = "Apache-2.0"

require swift-version.inc
require swift-source.inc
PV = "${SWIFT_VERSION}+git"

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

# Must match swift-bootstrap-native's prefix: swift-native reaches its
# bootstrap toolchain through this path and doesn't care which recipe put it
# there. native.bbclass's sstate-inputdirs is ${SYSROOT_DESTDIR}/${STAGING_DIR_NATIVE}/,
# so it has to sit inside ${STAGING_DIR_NATIVE} to be staged at all.
SWIFT_BOOTSTRAP_ROOT = "${STAGING_DIR_NATIVE}/opt/swift-bootstrap"

do_configure[network] = "0"
do_compile[network] = "0"

do_compile() {
    # build-script's toolchain discovery (utils/swift_build_support/.../toolchain.py)
    # searches PATH for clang/clang++ and sets cc = cxx = None if it finds
    # neither -- it never falls back to gcc. Point it at OE-core's clang-native
    # explicitly rather than depending on PATH order.
    export CC="${STAGING_BINDIR_NATIVE}/clang"
    export CXX="${STAGING_BINDIR_NATIVE}/clang++"

    # OE's native LDFLAGS (-Wl,--enable-new-dtags etc.) reach swiftc verbatim
    # through cmake, and swiftc rejects bare -Wl, flags. build-script manages
    # its own link options. Same reason as swift-native.bb.
    unset LDFLAGS
    unset BUILD_LDFLAGS

    cd ${S}
    ./utils/build-script \
        --preset-file=${SWIFT_PRESET_FILE} \
        --preset=yocto_stage0 \
        --host-cc=${STAGING_BINDIR_NATIVE}/clang \
        --host-cxx=${STAGING_BINDIR_NATIVE}/clang++ \
        install_destdir=${D} \
        -j ${BB_NUMBER_THREADS}
}

# do_compile wrote into ${D}/usr via build-script; base.bbclass's default
# do_install[cleandirs] = "${D}" would wipe it.
do_install[cleandirs] = ""

do_install() {
    install -d ${D}${SWIFT_BOOTSTRAP_ROOT}
    mv ${D}/usr ${D}${SWIFT_BOOTSTRAP_ROOT}/usr
}

PACKAGES = "${PN}"
FILES:${PN} = "${SWIFT_BOOTSTRAP_ROOT}"
SYSROOT_DIRS:append = " ${SWIFT_BOOTSTRAP_ROOT}"

INSANE_SKIP:${PN} = "already-stripped arch buildpaths dev-so file-rdeps libdir staticdev"

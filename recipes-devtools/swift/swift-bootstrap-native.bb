SUMMARY = "Prebuilt Swift toolchain, installed privately for bootstrapping swift-native"
DESCRIPTION = "Default provider for virtual/swift-bootstrap-native: the swift.org \
tarball staged under a private prefix. swift-stage0-native is the from-source \
alternative."
HOMEPAGE = "https://swift.org/install/"

require swift-native-tarball.inc
require swift-bootstrap-paths.inc

PROVIDES += "virtual/swift-bootstrap-native"

DEPENDS = "curl-native"
RDEPENDS:${PN} = "ncurses-native"

inherit native


PACKAGES = "${PN}"

do_install() {
    install -d ${D}${SWIFT_BOOTSTRAP_PREFIX}
    cp -rd ${S}/usr/bin ${D}${SWIFT_BOOTSTRAP_PREFIX}/
    cp -rd ${S}/usr/lib ${D}${SWIFT_BOOTSTRAP_PREFIX}/
    cp -rd ${S}/usr/include ${D}${SWIFT_BOOTSTRAP_PREFIX}/
    cp -rd ${S}/usr/share ${D}${SWIFT_BOOTSTRAP_PREFIX}/
}

FILES:${PN} = "${SWIFT_BOOTSTRAP_DIR}"
SYSROOT_DIRS:append = " ${SWIFT_BOOTSTRAP_DIR}"

INSANE_SKIP:${PN} = "already-stripped arch buildpaths dev-so file-rdeps libdir staticdev"

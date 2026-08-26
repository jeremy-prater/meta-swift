SUMMARY = "Prebuilt Swift toolchain, installed privately for bootstrapping swift-native"
DESCRIPTION = "Default provider for virtual/swift-bootstrap-native: the swift.org \
tarball staged under a private prefix. swift-stage0-native is the from-source \
alternative."
HOMEPAGE = "https://swift.org/install/"

require swift-native-tarball.inc

PROVIDES += "virtual/swift-bootstrap-native"

DEPENDS = "curl-native"
RDEPENDS:${PN} = "ncurses-native"

inherit native

# native.bbclass sets sstate-inputdirs to ${SYSROOT_DESTDIR}/${STAGING_DIR_NATIVE}/,
# so anything staged outside that path is silently dropped. Prefix all the
# bootstrap install paths with ${STAGING_DIR_NATIVE} to stay inside it.
SWIFT_BOOTSTRAP_PREFIX = "${STAGING_DIR_NATIVE}/opt/swift-bootstrap/usr"

PACKAGES = "${PN}"

do_install() {
    install -d ${D}${SWIFT_BOOTSTRAP_PREFIX}
    cp -rd ${S}/usr/bin ${D}${SWIFT_BOOTSTRAP_PREFIX}/
    cp -rd ${S}/usr/lib ${D}${SWIFT_BOOTSTRAP_PREFIX}/
    cp -rd ${S}/usr/include ${D}${SWIFT_BOOTSTRAP_PREFIX}/
    cp -rd ${S}/usr/share ${D}${SWIFT_BOOTSTRAP_PREFIX}/
}

FILES:${PN} = "${STAGING_DIR_NATIVE}/opt/swift-bootstrap"
SYSROOT_DIRS:append = " ${STAGING_DIR_NATIVE}/opt/swift-bootstrap"

INSANE_SKIP:${PN} = "already-stripped arch buildpaths dev-so file-rdeps libdir staticdev"

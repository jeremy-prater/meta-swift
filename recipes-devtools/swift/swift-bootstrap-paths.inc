# The staging contract between the virtual/swift-bootstrap-native providers
# (swift-bootstrap-native, swift-stage0-native) and their consumer
# (swift-native): whichever provider wins stages a complete host Swift
# toolchain under this private prefix, and swift-native finds it there without
# knowing which recipe put it there.
#
# It must live inside ${STAGING_DIR_NATIVE} because native.bbclass's
# sstate-inputdirs is ${SYSROOT_DESTDIR}/${STAGING_DIR_NATIVE}/ -- anything
# staged outside that path is silently dropped.
SWIFT_BOOTSTRAP_DIR = "${STAGING_DIR_NATIVE}/opt/swift-bootstrap"
SWIFT_BOOTSTRAP_PREFIX = "${SWIFT_BOOTSTRAP_DIR}/usr"
SWIFT_BOOTSTRAP_PATH = "${SWIFT_BOOTSTRAP_PREFIX}/bin"

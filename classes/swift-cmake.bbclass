inherit swift-cmake-base

DEPENDS += "swift-stdlib"

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux"

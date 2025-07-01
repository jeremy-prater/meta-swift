inherit swift-cmake-base

DEPENDS:append = " swift-stdlib libdispatch swift-foundation"

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux"

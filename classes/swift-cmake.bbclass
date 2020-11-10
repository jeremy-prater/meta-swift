inherit swift-cmake-base

DEPENDS += "swift-stdlib"

TARGET_LDFLAGS += "-L${WORKDIR}/recipe-sysroot/usr/lib/swift/linux"

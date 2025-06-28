# avoid conflicts with meta-clang
TOOLCHAIN = "gcc"

# appears to cause segfault
TARGET_CC_ARCH:remove:aarch64 = "-mbranch-protection=standard"

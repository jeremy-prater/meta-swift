# avoid conflicts with meta-clang
TOOLCHAIN = "gcc"

# appears to cause segfault
TARGET_CC_ARCH:remove:aarch64 = "-mbranch-protection=standard"

# workaround for building on x86_64: SSE appears to cause cyclic header
# dependency when building C++ std module. This needs investigation and an
# upstream fix
TARGET_CC_ARCH:remove:x86-64 = "-march=core2"
TARGET_CC_ARCH:remove:x86-64 = "-mtune=core2"
TARGET_CC_ARCH:remove:x86-64 = "-msse3"
TARGET_CC_ARCH:remove:x86-64 = "-mfpmath=sse"

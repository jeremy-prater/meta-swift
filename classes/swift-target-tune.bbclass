# force disable GLIBC_64BIT_TIME_FLAGS until SwiftNIO and other common packages
# are updated to support 64-bit time_t on 32-bit systems. ABI incompatibility
# may cause your program to crash; you have been warned.
TARGET_CC_ARCH:remove:arm = "${GLIBC_64BIT_TIME_FLAGS}"

# appears to cause segfault
TARGET_CC_ARCH:remove:aarch64 = "-mbranch-protection=standard"

# workaround for building on x86_64: SSE appears to cause cyclic header
# dependency when building C++ std module. This needs investigation and an
# upstream fix
TARGET_CC_ARCH:remove:x86-64 = "-march=core2"
TARGET_CC_ARCH:remove:x86-64 = "-mtune=core2"
TARGET_CC_ARCH:remove:x86-64 = "-msse3"
TARGET_CC_ARCH:remove:x86-64 = "-mfpmath=sse"

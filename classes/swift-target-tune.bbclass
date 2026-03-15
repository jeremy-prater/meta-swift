# appears to cause segfault
TARGET_CC_ARCH:remove:aarch64 = "-mbranch-protection=standard"

# workaround for building on x86_64: SSE appears to cause cyclic header
# dependency when building C++ std module. This needs investigation and an
# upstream fix
TARGET_CC_ARCH:remove:x86-64 = "-march=core2"
TARGET_CC_ARCH:remove:x86-64 = "-mtune=core2"
TARGET_CC_ARCH:remove:x86-64 = "-msse3"
TARGET_CC_ARCH:remove:x86-64 = "-mfpmath=sse"

python () {
    import shlex

    def expand_swiftc_cc_flags(flags):
        flags = [['-Xcc', flag] for flag in flags]
        return sum(flags, [])

    def concat_cmake_flags(flags):
        return " ".join(flags)

    def concat_destination_flags(flags):
        flags = [f'"{flag}"' for flag in flags]
        return ", ".join(flags)
    # ensure target-specific tune CC flags are propagated to clang and swiftc.
    # Note we are not doing this at present for LD flags, as there are none in
    # the architectures we support (and it would make the string expansion more
    # complicated).
    target_cc_arch = shlex.split(d.getVar("TARGET_CC_ARCH"))

    swift_target = d.getVar("SWIFT_TARGET_NAME") or ""
    target_arch = d.getVar("TARGET_ARCH") or ""
    if target_arch == "arm" and "armv7" in swift_target:
        if "-mthumb" in target_cc_arch:
            target_cc_arch.remove("-mthumb")

    d.setVar("SWIFT_EXTRA_CC_FLAGS_DESTINATION", concat_destination_flags(target_cc_arch))

    d.setVar("SWIFT_EXTRA_SWIFTC_CC_FLAGS_DESTINATION", concat_destination_flags(expand_swiftc_cc_flags(target_cc_arch)))

    d.setVar("SWIFT_EXTRA_CC_FLAGS", concat_cmake_flags(target_cc_arch))

    d.setVar("SWIFT_EXTRA_SWIFTC_CC_FLAGS", concat_cmake_flags(expand_swiftc_cc_flags(target_cc_arch)))
}


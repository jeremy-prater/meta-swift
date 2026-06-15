# Ship the Swift toolchain in the extensible/standalone SDK so that swiftc
# and the bundled Swift Clang are on the SDK host's PATH once
# environment-setup-*.sh is sourced. Adding nativesdk-swift here is
# equivalent to setting TOOLCHAIN_HOST_TASK += "nativesdk-swift" in a distro
# conf, but keeps the wiring inside meta-swift so it activates automatically
# for any user of this layer.
RDEPENDS:${PN} += "nativesdk-swift"

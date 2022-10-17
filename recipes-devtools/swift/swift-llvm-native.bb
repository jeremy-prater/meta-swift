SUMMARY = "Compiled Swift LLVM for Linux"
HOMEPAGE = "https://github.com/apple/llvm-project"

LICENSE="CLOSED"
LIC_FILES_CHKSUM=""

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_DIR = "llvm-swift"
SRC_URI = "https://github.com/colemancda/swift-armv7/releases/download/0.4.0/llvm-swift.zip"
SRC_URI[sha256sum] = "84c21ce101c6627f07d90f10df0448d4cb2848e6e2f96e544019207467dffca7"

inherit native

SYSROOT_DIRS_NATIVE += "${base_prefix}/opt"
S = "${WORKDIR}/${SRC_DIR}"

do_install_append () {
    mkdir -p ${D}${base_prefix}/opt/usr/lib/llvm-swift
    cp -rf ${S}/../bin ${D}${base_prefix}/opt/usr/lib/llvm-swift/
    cp -rf ${S}/../include ${D}${base_prefix}/opt/usr/lib/llvm-swift/
    cp -rf ${S}/../lib ${D}${base_prefix}/opt/usr/lib/llvm-swift/
    cp -rf ${S}/../share ${D}${base_prefix}/opt/usr/lib/llvm-swift/
}

FILES_${PN} += "${base_prefix}/opt/*"

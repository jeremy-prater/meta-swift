DESCRIPTION = "swift 5.1.2 - ARM v7 Binaries"
HOMEPAGE = "https://swift.org/download/#releases"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=2380e856fbdbc7ccae6bd699d53ec121"

SOURCE_FILE_ARM = "swift-5.1.2-armv7-Ubuntu1604.tgz"

RDEPENDS_${PN} = " libicuuc-swift libicui18n-swift"

SRC_URI = "https://github.com/uraimo/buildSwiftOnARM/releases/download/5.1.2/${SOURCE_FILE_ARM};unpack=0;name=arm \
           file://fix_modulemap.sh \
           file://LICENSE.txt \
"

SRC_URI[arm.sha256sum] = "ef2e9e282486e380b0d2ba604c003ceb9873ae477e8f0b97e242b27d720f5a61"

S="${WORKDIR}"

INSANE_SKIP_${PN} += "ldflags staticdev dev-so dev-elf "
INSANE_SKIP_${PN}-dev += "ldflags staticdev dev-so dev-elf "
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT  = "1"

do_install() {

    echo "Installing ${DESCRIPTION} ..."

    install -d ${D}${bindir}

    tar -xvzf ${WORKDIR}/${SOURCE_FILE_ARM} -C ${D}${bindir}/../..

    echo "Fixing module map"
    ${WORKDIR}/fix_modulemap.sh ${D}${bindir}/../lib/swift/linux/armv7/glibc.modulemap
    
    rm -rf ${D}${libdir}/python2.7
    rm -rf ${D}${libdir}/lldb/clang
    rm -rf ${D}${libdir}/lldb
    rm -rf ${D}${libdir}/liblldb*
    rm -rf ${D}${libdir}/lib_InternalSwiftSyntaxParser.so
    rm -rf ${D}${libdir}/swift/migrator
    rm -rf ${D}${libdir}/swift/pm
    rm -rf ${D}/usr/libexec
    rm -rf ${D}/CONTROL
    rm -rf ${D}${bindir}
}

SOLIBS = ".so"
FILES_SOLIBSDEV = ""

#FILES_${PN} = "*"
FILES_${PN}-dev_append = " ${libdir}/swift/Block"
FILES_${PN}-dev_append = " ${libdir}/swift/clang"
FILES_${PN}-dev_append = " ${libdir}/swift/C*"
FILES_${PN}-dev_append = " ${libdir}/swift/dispatch"
FILES_${PN}-dev_append = " ${libdir}/swift/linux/armv7"
FILES_${PN}-dev_append = " ${libdir}/swift/os"
FILES_${PN}-dev_append = " ${libdir}/swift/shims"
FILES_${PN}-dev_append = " ${libdir}/swift_static"



DESCRIPTION = "swift 5.1.2 - ARM v7 SDK"
HOMEPAGE = "https://swift.org/download/#releases"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=2380e856fbdbc7ccae6bd699d53ec121"

SOURCE_FILE_x86 = "swift-5.1.2-RELEASE-ubuntu16.04.tar.gz"
SOURCE_FILE_ARM = "swift-5.1.2-armv7-Ubuntu1604.tgz"


SRC_URI = "https://github.com/uraimo/buildSwiftOnARM/releases/download/5.1.2/${SOURCE_FILE_ARM};unpack=0;name=arm \
           https://swift.org/builds/swift-5.1.2-release/ubuntu1604/swift-5.1.2-RELEASE/${SOURCE_FILE_x86};unpack=0;name=x86 \
           file://fix_modulemap.sh \
           file://LICENSE.txt \
"

SRC_URI[arm.sha256sum] = "ef2e9e282486e380b0d2ba604c003ceb9873ae477e8f0b97e242b27d720f5a61"
SRC_URI[x86.sha256sum] = "bc57c6730d22099e884e0327abcbc2f090a779d3024d1351f441b6520f7dc41f"

INSANE_SKIP_${PN} += "ldflags staticdev dev-so dev-elf "
INHIBIT_PACKAGE_STRIP = "1"
INHIBIT_SYSROOT_STRIP = "1"
INHIBIT_PACKAGE_DEBUG_SPLIT  = "1"

inherit native

S = "${WORKDIR}"

do_install() {

    echo "Installing ${DESCRIPTION} ..."

    install -d ${D}${bindir}
    install -d ${D}${bindir}/../../opt/swift-arm
    install -d ${D}${bindir}/../../opt/swift-tmp

    # Install SPM in the rootfs
    tar -xzf ${WORKDIR}/${SOURCE_FILE_x86} --strip-components=1 -C ${D}${bindir}/../../
    
    # Create the cross-compiling swiftc
    tar -xzf ${WORKDIR}/${SOURCE_FILE_x86} --strip-components=1 -C ${D}${bindir}/../../opt/swift-arm
    tar -xzf ${WORKDIR}/${SOURCE_FILE_ARM} -C ${D}${bindir}/../../opt/swift-tmp


    echo "Copying swift arm std lib to SDK"
    cp -rav ${D}${bindir}/../../opt/swift-tmp/usr/lib/swift/linux ${D}${bindir}/../../opt/swift-arm/usr/lib/swift/
    cp -rav ${D}${bindir}/../../opt/swift-tmp/usr/lib/swift_static/linux ${D}${bindir}/../../opt/swift-arm/usr/lib/swift/

    echo "Fixing module map"
    ${WORKDIR}/fix_modulemap.sh ${D}${bindir}/../../opt/swift-arm/usr/lib/swift/linux/armv7/glibc.modulemap
}

do_populate_sysroot_append() {
    import os  
    import shutil 

    destdir = d.getVar('SYSROOT_DESTDIR')
    ddir = d.getVar('D')
    bindir = d.getVar('bindir')
    # print ("do_populate_sysroot_append --> Destdir " + destdir)
    # print ("do_populate_sysroot_append --> ddir " + ddir)
    # print ("do_populate_sysroot_append --> bindir " + bindir)

    shutil.copytree(ddir + bindir + "/../../opt/swift-arm", destdir + bindir + "/../../opt/swift-arm")
}

FILES_${PN} = "*"

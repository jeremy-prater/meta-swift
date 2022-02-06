SUMMARY = "Swift toolchain for x86_64 linux"
HOMEPAGE = "https://swift.org/download/#releases"
LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${S}/usr/share/swift/LICENSE.txt;md5=f6c482a0548ea60d6c2e015776534035"

require swift-version.inc
PV = "${SWIFT_VERSION}"

#######################################################################
# We use our own Swift 5.3 x86_64 Linux toolchain in order to link    #
# against the libgcc runtime in our ARM code.                         #
#######################################################################
#SRC_DIR = "."
#SRC_URI = "https://rpe-downloads-dev.s3-us-west-2.amazonaws.com/swift/swift-${PV}-ubuntu20.04-patched.tar.gz"
#SRC_URI[sha256sum] = "47f9ff1fe255b0c40dadb8c66e356a6be75f55c34f3907e9840c8ff9da6ea5fe"


#######################################################################
# Once our own toolchain is no longer needed switch back to this src. #
#######################################################################
SRC_DIR = "swift-${PV}-RELEASE-ubuntu20.04"
SRC_URI = "https://swift.org/builds/swift-${PV}-release/ubuntu2004/swift-${PV}-RELEASE/${SRC_DIR}.tar.gz"
#SRC_URI[sha256sum] = "5ac1fb9b8963e1c44f541f55cbf6cc10faefb1f21598d813f14f8aaeb22b1d80"

DEPENDS = "curl"
RDEPENDS = "ncurses-native"

S = "${WORKDIR}/${SRC_DIR}"

inherit native


########################################################################
# This informs bitbake that we want to install a non-default directory #
# in the native sysroot.                                               #
#                                                                      #
# We install the swift toolchain into opt to avoid conflicts with      #
# other packages when installing. Ex:                                  #
#                                                                      #
#   The file /usr/include/unicode/sortkey.h is installed by both       #
#   swift-native and icu-native, aborting                              #
#                                                                      #
########################################################################
SYSROOT_DIRS_NATIVE += "${base_prefix}/opt"

do_install_append () {
    install -d ${D}${base_prefix}/opt/usr/bin/
    cp -r ${S}/usr/bin/* ${D}${base_prefix}/opt/usr/bin/

    install -d ${D}${bindir}/
    ln -s ../../opt/usr/bin ${D}${bindir}/swift-tools

    install -d ${D}${base_prefix}/opt/usr/lib/
    cp -r ${S}/usr/lib/* ${D}${base_prefix}/opt/usr/lib/

    install -d ${D}${base_prefix}/opt/usr/include/
    cp -r ${S}/usr/include/* ${D}${base_prefix}/opt/usr/include/

    install -d ${D}${base_prefix}/opt/usr/share/
    cp -r ${S}/usr/share/* ${D}${base_prefix}/opt/usr/share/
}

FILES_${PN} += "${base_prefix}/opt/*"

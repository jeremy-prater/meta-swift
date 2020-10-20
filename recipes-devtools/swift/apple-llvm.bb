SUMMARY = "Apple fork of LLVM"
HOMEPAGE = "https://github.com/apple/llvm-project"
DESCRIPTION = "This recipe provides the minimum LLVM files required by the swift-stdlib."

LICENSE = "Apache-2.0-with-LLVM-exception" 
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=8a15a0759ef07f2682d2ba4b893c9afe"

PV = "5.3"

SRC_URI = "git://github.com/apple/llvm-project;branch=${SRCBRANCH}"
SRCBRANCH = "swift/release/${PV}"
SRCREV = "c39a810ec308dd4a8d93c5011fb73a5c987e8680"

S = "${WORKDIR}/git"

do_install() {
    install -d ${D}${libdir}/apple-llvm
    #cp -r ${S}/llvm/cmake ${D}${libdir}/apple-llvm
    cp -r ${S}/llvm/include ${D}${libdir}/apple-llvm

    #install -d ${D}${libdir}/apple-llvm/lib
}

FILES_${PN}-dev = " \
   ${libdir}/apple-llvm/* \
"

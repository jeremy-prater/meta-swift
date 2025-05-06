SUMMARY = "International Component for Unicode libraries"
DESCRIPTION = "The International Component for Unicode (ICU) is a mature, \
portable set of C/C++ and Java libraries for Unicode support, software \
internationalization (I18N) and globalization (G11N), giving applications the \
same results on all platforms."
HOMEPAGE = "http://site.icu-project.org/"

LICENSE = "ICU"
DEPENDS = "icu-swift-native"
DEPENDS_class-native = ""

S = "${WORKDIR}/icu/source"
SPDX_S = "${WORKDIR}/icu"
STAGING_ICU_DIR_NATIVE = "${STAGING_DATADIR_NATIVE}/${BPN}/${PV}"

CPPFLAGS:append_libc-uclibc = " -DU_TIMEZONE=0"

inherit autotools pkgconfig

# ICU needs the native build directory as an argument to its --with-cross-build option when
# cross-compiling. Taken the situation that different builds may share a common sstate-cache
# into consideration, the native build directory needs to be staged.
EXTRA_OECONF = "--with-cross-build=${STAGING_ICU_DIR_NATIVE}"
EXTRA_OECONF_class-native = ""
EXTRA_OECONF_class-nativesdk = "--with-cross-build=${STAGING_ICU_DIR_NATIVE}"

PREPROCESS_RELOCATE_DIRS = "${datadir}/${BPN}/${PV}"
do_install:append_class-native() {
	mkdir -p ${D}/${STAGING_ICU_DIR_NATIVE}/config
	cp -r ${B}/config/icucross.mk ${D}/${STAGING_ICU_DIR_NATIVE}/config
	cp -r ${B}/config/icucross.inc ${D}/${STAGING_ICU_DIR_NATIVE}/config
	cp -r ${B}/lib ${D}/${STAGING_ICU_DIR_NATIVE}
	cp -r ${B}/bin ${D}/${STAGING_ICU_DIR_NATIVE}
	cp -r ${B}/tools ${D}/${STAGING_ICU_DIR_NATIVE}

    rm -r ${D}${STAGING_BINDIR_NATIVE}
    rm -r ${D}${STAGING_SBINDIR_NATIVE}
    rm -r ${D}${STAGING_LIBDIR_NATIVE}
    rm -r ${D}${STAGING_DATADIR_NATIVE}/icu
}

do_install:append() {
    rm -rf ${D}/usr/lib/icu
    rm -rf ${D}/usr/share/icu
    rm -f ${D}/${libdir}/*.so
    rm -rf ${D}/${bindir}
    rm -rf ${D}/usr/sbin
    rm -rf ${D}/${includedir}
    rm -rf ${D}/usr/lib/pkgconfig
}

PACKAGES =+ "libicudata-swift libicuuc-swift libicui18n-swift libicule-swift libiculx-swift libicutu-swift libicuio-swift"

FILES_${PN}-dev += "${libdir}/${BPN}/"

FILES_libicudata-swift = "${libdir}/libicudata.so.*"
FILES_libicuuc-swift = "${libdir}/libicuuc.so.*"
FILES_libicui18n-swift = "${libdir}/libicui18n.so.*"
FILES_libicule-swift = "${libdir}/libicule.so.*"
FILES_libiculx-swift = "${libdir}/libiculx.so.*"
FILES_libicutu-swift = "${libdir}/libicutu.so.*"
FILES_libicuio-swift = "${libdir}/libicuio.so.*"

BBCLASSEXTEND = "native"


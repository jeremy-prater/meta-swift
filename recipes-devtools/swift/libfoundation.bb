SUMMARY = "The Foundation framework defines a base layer of functionality that is required for almost all applications."
HOMEPAGE = "https://github.com/apple/swift-corelibs-foundation"

LICENSE = "Apache-2.0" 
LIC_FILES_CHKSUM = "file://LICENSE;md5=1cd73afe3fb82e8d5c899b9d926451d0"

require swift-version.inc
PV = "${SWIFT_VERSION}"

SRC_URI = "git://github.com/apple/swift-corelibs-foundation;branch=${SRCBRANCH} \
           file://0001-Make-FoundationConfig.cmake-not-depend-on-build-dir.patch \
           "
SRCBRANCH = "release/${PV}"
SRCREV = "dfb10f7f74b73ba5742f3defcbb4d011abe9f2d4"

S = "${WORKDIR}/git"

DEPENDS = "swift-stdlib libdispatch ncurses libxml2 icu curl"

inherit swift-cmake-base

TARGET_LDFLAGS += "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux"

# Enable Swift parts
EXTRA_OECMAKE += "-DENABLE_SWIFT=YES"

EXTRA_OECMAKE += '-DCMAKE_Swift_FLAGS="${SWIFT_FLAGS}"'
EXTRA_OECMAKE += '-DCMAKE_VERBOSE_MAKEFILE=ON'
EXTRA_OECMAKE += '-DCF_DEPLOYMENT_SWIFT=ON'
lcl_maybe_fortify="-D_FORTIFY_SOURCE=0"

EXTRA_OECMAKE+= "-Ddispatch_DIR=${STAGING_DIR_TARGET}/usr/lib/swift/dispatch/cmake"

# Ensure the right CPU is targeted
TARGET_CPU_NAME = "armv7-a"
cmake_do_generate_toolchain_file_append() {
    sed -i 's/set([ ]*CMAKE_SYSTEM_PROCESSOR .*[ ]*)/set(CMAKE_SYSTEM_PROCESSOR ${TARGET_CPU_NAME})/' ${WORKDIR}/toolchain.cmake
}

do_install_append() {
    # No need to install the plutil onto the target, so remove it for now
    rm ${D}${bindir}/plutil

    # Since plutil was the only thing in the bindir, remove the bindir as well
    rmdir ${D}${bindir}
}

FILES_${PN} = "\
  ${libdir}/swift/linux/libFoundationXML.so \
  ${libdir}/swift/linux/libFoundation.so \
  ${libdir}/swift/linux/libFoundationNetworking.so \
"

FILES_${PN}-dev = "\
  ${libdir}/swift/Foundation/cmake/FoundationConfig.cmake \
  ${libdir}/swift/Foundation/cmake/FoundationExports.cmake \
  ${libdir}/swift/Foundation/cmake/FoundationExports-noconfig.cmake \
  ${libdir}/swift/Foundation/cmake/FoundationNetworkingExports.cmake \
  ${libdir}/swift/Foundation/cmake/FoundationNetworkingExports-noconfig.cmake \
  ${libdir}/swift/Foundation/cmake/FoundationXMLExports.cmake \
  ${libdir}/swift/Foundation/cmake/FoundationXMLExports-noconfig.cmake \
  ${libdir}/swift/CFURLSessionInterface/CFURLSessionInterface.h \
  ${libdir}/swift/CFURLSessionInterface/module.map \
  ${libdir}/swift/CoreFoundation/CFAvailability.h \
  ${libdir}/swift/CoreFoundation/CFPlugIn.h \
  ${libdir}/swift/CoreFoundation/CFBase.h \
  ${libdir}/swift/CoreFoundation/CFUUID.h \
  ${libdir}/swift/CoreFoundation/CFSet.h \
  ${libdir}/swift/CoreFoundation/CFNumber.h \
  ${libdir}/swift/CoreFoundation/CFDictionary.h \
  ${libdir}/swift/CoreFoundation/CFCharacterSetPriv.h \
  ${libdir}/swift/CoreFoundation/CFRunArray.h \
  ${libdir}/swift/CoreFoundation/CFSocket.h \
  ${libdir}/swift/CoreFoundation/CFNotificationCenter.h \
  ${libdir}/swift/CoreFoundation/CFLogUtilities.h \
  ${libdir}/swift/CoreFoundation/CFByteOrder.h \
  ${libdir}/swift/CoreFoundation/CFBag.h \
  ${libdir}/swift/CoreFoundation/CoreFoundation.h \
  ${libdir}/swift/CoreFoundation/CFAttributedString.h \
  ${libdir}/swift/CoreFoundation/CFBitVector.h \
  ${libdir}/swift/CoreFoundation/CFDate.h \
  ${libdir}/swift/CoreFoundation/CFData.h \
  ${libdir}/swift/CoreFoundation/CFPriv.h \
  ${libdir}/swift/CoreFoundation/CFArray.h \
  ${libdir}/swift/CoreFoundation/CFRuntime.h \
  ${libdir}/swift/CoreFoundation/CFNumberFormatter.h \
  ${libdir}/swift/CoreFoundation/CFPreferences.h \
  ${libdir}/swift/CoreFoundation/CFURLAccess.h \
  ${libdir}/swift/CoreFoundation/CFUtilities.h \
  ${libdir}/swift/CoreFoundation/TargetConditionals.h \
  ${libdir}/swift/CoreFoundation/CFBinaryHeap.h \
  ${libdir}/swift/CoreFoundation/CFURLSessionInterface.h \
  ${libdir}/swift/CoreFoundation/module.map \
  ${libdir}/swift/CoreFoundation/CFPlugInCOM.h \
  ${libdir}/swift/CoreFoundation/CFRunLoop.h \
  ${libdir}/swift/CoreFoundation/CFDateFormatter.h \
  ${libdir}/swift/CoreFoundation/CFCalendar_Internal.h \
  ${libdir}/swift/CoreFoundation/CFError.h \
  ${libdir}/swift/CoreFoundation/CFBundle.h \
  ${libdir}/swift/CoreFoundation/CFTree.h \
  ${libdir}/swift/CoreFoundation/CFString.h \
  ${libdir}/swift/CoreFoundation/ForSwiftFoundationOnly.h \
  ${libdir}/swift/CoreFoundation/CFStream.h \
  ${libdir}/swift/CoreFoundation/CFLocking.h \
  ${libdir}/swift/CoreFoundation/CFURL.h \
  ${libdir}/swift/CoreFoundation/CFURLPriv.h \
  ${libdir}/swift/CoreFoundation/CFLocaleInternal.h \
  ${libdir}/swift/CoreFoundation/CFStreamPriv.h \
  ${libdir}/swift/CoreFoundation/CFCharacterSet.h \
  ${libdir}/swift/CoreFoundation/CFTimeZone.h \
  ${libdir}/swift/CoreFoundation/CFDateInterval.h \
  ${libdir}/swift/CoreFoundation/ForFoundationOnly.h \
  ${libdir}/swift/CoreFoundation/CFBundlePriv.h \
  ${libdir}/swift/CoreFoundation/CFMachPort.h \
  ${libdir}/swift/CoreFoundation/CFStringEncodingExt.h \
  ${libdir}/swift/CoreFoundation/CFPropertyList.h \
  ${libdir}/swift/CoreFoundation/CFRegularExpression.h \
  ${libdir}/swift/CoreFoundation/CFStringEncodingConverterExt.h \
  ${libdir}/swift/CoreFoundation/CFMessagePort.h \
  ${libdir}/swift/CoreFoundation/CFKnownLocations.h \
  ${libdir}/swift/CoreFoundation/CFStringEncodingConverter.h \
  ${libdir}/swift/CoreFoundation/CFCalendar.h \
  ${libdir}/swift/CoreFoundation/CFUserNotification.h \
  ${libdir}/swift/CoreFoundation/CFDateIntervalFormatter.h \
  ${libdir}/swift/CoreFoundation/CFLocale.h \
  ${libdir}/swift/CoreFoundation/CFURLComponents.h \
  ${libdir}/swift/CoreFoundation/CFDateComponents.h \
  ${libdir}/swift/linux/armv7/Foundation.swiftmodule \
  ${libdir}/swift/linux/armv7/FoundationXML.swiftmodule \
  ${libdir}/swift/linux/armv7/FoundationNetworking.swiftmodule \
  ${libdir}/swift/CFXMLInterface/CFXMLInterface.h \
  ${libdir}/swift/CFXMLInterface/module.map \
"

FILES_${PN}-doc = "\
  ${libdir}/swift/linux/armv7/FoundationXML.swiftdoc \
  ${libdir}/swift/linux/armv7/FoundationNetworking.swiftdoc \
  ${libdir}/swift/linux/armv7/Foundation.swiftdoc \
"

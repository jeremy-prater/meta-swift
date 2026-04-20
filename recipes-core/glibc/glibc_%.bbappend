FILESEXTRAPATHS:prepend := "${THISDIR}/${PN}:"

SRC_URI += "\
    file://0001-features.h-disable-_FORTIFY_SOURCE-for-Swift-Clang-i.patch \
    "

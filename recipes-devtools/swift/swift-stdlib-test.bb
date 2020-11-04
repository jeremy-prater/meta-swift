SUMMARY = "Swift standard library testing package"

LICENSE = "CLOSED"

SRC_URI = "file://main.swift \
           file://CMakeLists.txt \
"

inherit swift

S = "${WORKDIR}"


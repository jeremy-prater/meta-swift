
DEPENDS += "swift-native glibc gcc libgcc swift-stdlib libdispatch libfoundation"

# Additional parameters to pass to SPM
EXTRA_OESWIFT ?= ""

python swift_do_configure() {
    import os
    import os.path
    import shutil

    workdir = d.getVar("WORKDIR", True)
    recipe_sysroot = d.getVar("STAGING_DIR_TARGET", True)

    cxx_include_base = recipe_sysroot + "/usr/include/c++"
    cxx_include_list = os.listdir(cxx_include_base)
    if len(cxx_include_list) != 1:
        bb.fatal("swift bbclass detected more than one c++ runtime, unable to determine which one to use")
    cxx_version = cxx_include_list[0]
    
    d.setVar('SWIFT_CXX_VERSION', cxx_version)

    swift_destination_template = """{
        "version":1,
        "sdk":"${STAGING_DIR_TARGET}/",
        "toolchain-bin-dir":"${STAGING_DIR_NATIVE}/usr/bin",
        "target":"armv7-unknown-linux-gnueabihf",
        "dynamic-library-extension":"so",
        "extra-cc-flags":[ 
            "-fPIC",
            "-I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_CXX_VERSION}",
            "-I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_CXX_VERSION}/${TARGET_SYS}",
            "-I${STAGING_DIR_NATIVE}/usr/lib/arm-poky-linux-gnueabi/gcc/arm-poky-linux-gnueabi/9.3.0/include",
            "-I${STAGING_DIR_NATIVE}/usr/lib/arm-poky-linux-gnueabi/gcc/arm-poky-linux-gnueabi/9.3.0/include-fixed"
        ],
        "extra-swiftc-flags":[ 
            "-target",
            "armv7-unknown-linux-gnueabihf",
            "-use-ld=lld",
            "-tools-directory",
            "/usr/bin",
            
            "-Xlinker", "-rpath", "-Xlinker", "/usr/lib/swift/linux",
            
            "-Xlinker",
            "-L${STAGING_DIR_TARGET}",

            "-Xlinker",
            "-L${STAGING_DIR_TARGET}/lib",

            "-Xlinker",
            "-L${STAGING_DIR_TARGET}/usr/lib",

            "-Xlinker",
            "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux",

            "-Xlinker",
            "-L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_CXX_VERSION}",
            
            "-I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_CXX_VERSION}",
            "-I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_CXX_VERSION}/${TARGET_SYS}",
            "-I${STAGING_DIR_NATIVE}/usr/lib/arm-poky-linux-gnueabi/gcc/arm-poky-linux-gnueabi/9.3.0/include",
            "-I${STAGING_DIR_NATIVE}/usr/lib/arm-poky-linux-gnueabi/gcc/arm-poky-linux-gnueabi/9.3.0/include-fixed",

            "-resource-dir", "${STAGING_DIR_TARGET}/usr/lib/swift",
            "-module-cache-path", "${B}/ModuleCache",
            "-Xclang-linker", "-B${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/9.3.0",
            "-Xclang-linker", "-B${STAGING_DIR_TARGET}/usr/lib",

            "-sdk", "${STAGING_DIR_TARGET}"
        ],
        "extra-cpp-flags":[ 
            "-lstdc++",
        ]
    }"""

    bb.warn(d.getVar("TARGET_SYS", True))

    swift_destination =  d.expand(swift_destination_template)

    d.delVar("SWIFT_CXX_VERSION")
    
    configJSON = open(workdir + "/destination.json", "w")
    configJSON.write(swift_destination)
    configJSON.close()
}

swift_do_compile()  {
    cd ${S}
    
    swift build --build-path ${B} -v -c release --destination ${WORKDIR}/destination.json ${EXTRA_OESWIFT}
}

EXPORT_FUNCTIONS do_configure do_compile


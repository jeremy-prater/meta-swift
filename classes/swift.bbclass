
DEPENDS_prepend += "swift-native swift glibc gcc libgcc "
RDEPENDS_${PN} += " swift "

# Additional parameters to pass to SPM
EXTRA_OESWIFT ?= ""

def fix_socket_header(filename):
    with open(filename, 'r') as f:
        lines = f.readlines() 

    os.remove(filename)

    with open(filename, 'w') as f:
        for line in lines:
            if line.startswith('#define SO_RCVTIMEO ') and ("SO_RCVTIMEO_OLD" in line) and ("?" in line):
                f.write('#define SO_RCVTIMEO		SO_RCVTIMEO_OLD\n')
            elif line.startswith('#define SO_SNDTIMEO ') and ("SO_SNDTIMEO_OLD" in line) and ("?" in line):
                f.write('#define SO_SNDTIMEO		SO_SNDTIMEO_OLD\n')
            else:
                f.write(line)

python swift_do_configure() {
    import os
    import os.path
    import shutil
    workdir = d.getVar("WORKDIR", True)
    recipe_sysroot = d.getVar("STAGING_DIR_TARGET", True)
    
    #socket.h workaround. Seems that SO_RCVTIMEO and SO_SNDTIMEO definitions using ? aren't working
    socket_header = recipe_sysroot + "/usr/include/asm-generic/socket.h"
    fix_socket_header(socket_header)

    cxx_include_base = recipe_sysroot + "/usr/include/c++"
    cxx_include_list = os.listdir(cxx_include_base)
    if len(cxx_include_list) != 1:
        bb.fatal("swift bbclass detected more than one c++ runtime, unable to determine which one to use")
    cxx_version = cxx_include_list[0]
    
    d.setVar('SWIFT_CXX_VERSION', cxx_version)

    swift_destination_template = """{
        "version":1,
        "sdk":"${STAGING_DIR_TARGET}/",
        "toolchain-bin-dir":"${STAGING_DIR_NATIVE}/opt/swift-arm/usr/bin",
        "target":"armv7-unknown-linux-gnueabihf",
        "dynamic-library-extension":"so",
        "extra-cc-flags":[ 
            "-fPIC",
            "-I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_CXX_VERSION}",
            "-I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_CXX_VERSION}/${TARGET_SYS}",
        ],
        "extra-swiftc-flags":[ 
            "-target",
            "armv7-unknown-linux-gnueabihf",
            "-use-ld=lld",
            "-tools-directory",
            "/usr/bin",
            
            "-Xlinker", "-rpath", "-Xlinker", "/usr/lib/swift/linux",

            "-Xlinker",
            "-L${STAGING_DIR_TARGET}/lib",

            "-Xlinker",
            "-L${STAGING_DIR_TARGET}/usr/lib",

            "-Xlinker",
            "-L${STAGING_DIR_TARGET}/usr/lib/swift/linux",

            "-Xlinker",
            "-L${STAGING_DIR_TARGET}/usr/lib/${TARGET_SYS}/${SWIFT_CXX_VERSION}",
            
            "-I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_CXX_VERSION}",
            "-I${STAGING_DIR_TARGET}/usr/include/c++/${SWIFT_CXX_VERSION}/${TARGET_SYS}"
        ],
        "extra-cpp-flags":[ 
            "-lstdc++"
        ]
    }"""

    swift_destination =  d.expand(swift_destination_template)

    d.delVar("SWIFT_CXX_VERSION")
    
    configJSON = open(workdir + "/destination.json", "w")
    configJSON.write(swift_destination)
    configJSON.close()
}

swift_do_compile()  {
    #Linker isn't finding crtbeginS.o and crtendS.o under ${TARGET_SYS} path
    cp -r ${WORKDIR}/recipe-sysroot/usr/lib/${TARGET_SYS}/*/* ${WORKDIR}/recipe-sysroot/usr/lib
	cd ${S}
    ${WORKDIR}/recipe-sysroot-native/usr/bin/swift build -v -c release --destination ${WORKDIR}/destination.json ${EXTRA_OESWIFT}
}

EXPORT_FUNCTIONS do_configure do_compile

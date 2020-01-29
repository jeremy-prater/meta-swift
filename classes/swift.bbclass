# Path to the dotnet project
OECMAKE_SOURCEPATH ??= "${S}"

DEPENDS_prepend += "swift-native dbus swift glibc gcc libgcc"
RDEPENDS_${PN} += "swift "

python swift_do_configure() {
    import os
    import os.path
    WORKDIR = d.getVar("WORKDIR", True)

    SWIFT_CONFIG = """{
        "version":1,
        "sdk":"%s/recipe-sysroot/",
        "toolchain-bin-dir":"%s/recipe-sysroot-native/opt/swift-arm/usr/bin",
        "target":"armv7-unknown-linux-gnueabihf",
        "dynamic-library-extension":"so",
        "extra-cc-flags":[ 
            "-fPIC"
        ],
        "extra-swiftc-flags":[ 
            "-target",
            "armv7-unknown-linux-gnueabihf",
            "-use-ld=lld",
            "-tools-directory",
            "/usr/bin",

            "-Xlinker",
            "-L%s/recipe-sysroot/lib",

            "-Xlinker",
            "-L%s/recipe-sysroot/usr/lib",

            "-Xlinker",
            "-L%s/recipe-sysroot/usr/lib/swift/linux",

            "-I%s/recipe-sysroot/usr/include/dbus-1.0"
        ],
        "extra-cpp-flags":[ 
            "-lstdc++"
        ]
    }""" % (
        WORKDIR,
        WORKDIR,
        WORKDIR,
        WORKDIR,
        WORKDIR,
        WORKDIR        
    )
    print(WORKDIR)
    print(SWIFT_CONFIG)
    configJSON = open(WORKDIR + "/destination.json", "w")
    configJSON.write(SWIFT_CONFIG)
    configJSON.close()
}

swift_do_compile()  {
    # Move required shared libraries into a common library path
    cp -r ${WORKDIR}/recipe-sysroot/usr/lib/arm-savant-linux-gnueabi/6.3.0/* ${WORKDIR}/recipe-sysroot/usr/lib

	cd ${WORKDIR}
    rm -rf ./.build
    ./recipe-sysroot-native/usr/bin/swift build -v -c release --destination destination.json
}

EXPORT_FUNCTIONS do_configure do_compile

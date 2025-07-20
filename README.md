# meta-swift

meta-swift is a [Yocto](https://www.yoctoproject.org) layer for the [Swift](https://www.swift.org) programming language.

This layer presently supports the latest version of Swift (6.1.2) for several Yocto versions. It should also be compatible with earlier versions of Swift, after updating the version in [swift-version.inc](recipes-devtools/swift/swift-version.inc). (This may also require changes to [swift-native.bb](recipes-devtools/swift/swift-native.bb) to reflect the different install artifacts.)

Both `x86_64` and `aarch64` host architectures are supported. The layer follows the convention of matching branch names with their corresponding Yocto release.

The meta-swift layer has been tested with the following Yocto target machines:

- `qemuarm`
- `qemuarm64`
- `qemux86-64`
- `beaglebone-yocto`
- `raspberrypi-armv7`
- `raspberrypi-armv8`
- `raspberrypi4-64`

Other machines that use `x86_64`, `armv7` or `aarch64` target architectures should also work.

## Compiling

A good way to get started is to look at the [meta-swift-examples](https://github.com/xavgru12/meta-swift-examples) repository, which contains scripts and a workspace for building under Docker.

The local [CI workflows](.github/workflows/build.yml) also provide some examples of how the layer is compiled.

## Usage

First, add the meta-swift layer to your project, by checking out the appropriate branch for your Yocto version (e.g. scarthgap) and using `bitbake-layers add-layer`. (You may also modify `bblayers.conf` directly.)

Create a new Swift package and include it in your BitBake recipe as follows:

```bash
DESCRIPTION = "An example Swift application"
LICENSE = "CLOSED"

SRC_URI = "\
    file://Sources/hello-world/main.swift \
    file://Package.swift \
"

S = "${SWIFT_UNPACKDIR}"
B = "${WORKDIR}/build"

inherit swift
```

When you inherit the `swift` class, BitBake does the following:

- Automatically downloads the Swift toolchain for the host architecture and creates a cross-compiling sysroot
- Adds build dependencies for the Swift standard libraries, including Foundation
- Performs the required build steps to build a Swift package

By default, Swift tests are not built. To build them, add:

```bash
SWIFT_BUILD_TESTS = "1"
```

to your recipe.

Note that Yocto will automatically detect and add runtime dependencies for the Swift runtime, so it is not necessary to add them explicitly in your package.

## Deployment

The user of this layer must provide their own `do_install` function for swift packages. An example of this is available in `swift-hello-world.bb`:

```bash
do_install() {
    install -d ${D}${bindir}
    install -m 0755 ${BUILD_DIR}/hello-world ${D}${bindir}
    install -m 0755 ${BUILD_DIR}/hello-worldPackageTests.xctest ${D}${bindir}
}

INSANE_SKIP:${PN} = "buildpaths"
INSANE_SKIP:${PN}-dbg = "buildpaths"
```

The finished binaries are located in ${BUILD\_DIR}. Skipping `buildpaths` package QA is required on styhead and higher, as these warnings are treated as errors (and the build directory path is often embedded in Swift binaries, a [known issue](https://github.com/jeremy-prater/meta-swift/issues/28)).

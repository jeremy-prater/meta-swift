# meta-swift

Yocto layer for the Swift programming language.

# Usage

Add this layer to your project (refer to Yocto user manual, or use `bitbake-layers add-layer`).

Create a new Swift application and include it in your build as follows:

```
DESCRIPTION = "My Swift app"
LICENSE = "CLOSED"

SRC_URI = "file://Sources/hello-world/main.swift \
           file://Package.swift \
"

inherit swift
```

When you `inherit swift` class, it does the following:

- Automatically download the x86\_64 SDK binaries and create a cross-compiling sysroot
- Add an RDEPENDS:${PN} for `swift`
- Performs the required build steps

# Deployment

The user of this layer must provide their own `do_install` function.

The finished binaries are located in ${BUILD\_DIR}.

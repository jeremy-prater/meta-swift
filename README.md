# meta-swift
Yocto meta-layer for swift-on-arm (Swift 6.0.2)

# Usage

Add this meta layer to your project (refer to yocto user manual)

Create a new swift application and include it in your yocto build as follows...

```
DESCRIPTION = "My swift 6.0.2 app"
LICENSE = "CLOSED"

SRC_URI = "file://Sources/hello-world/main.swift \
           file://Package.swift \
"

inherit swift
```

This does a few things, when you `inherit swift` meta-layer class, it will does the following...

- Automatically download the x86\_64 and ARMv7 swift 6.0.2 binaries and create a cross-compiling sys-root
- Add an RDEPENDS_${PN} for `swift` which is the Armv7 runtime
- Performs the required build steps

# Deployment

The user of this meta-layer must provide their own `do_install` function.

The finished binaries are located in ${WORKDIR}/.build/release/*

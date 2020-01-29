#!/bin/bash

# See https://github.com/CSCIX65G/SwiftCrossCompilers/blob/master/build_cross_compiler#L34
# This is a funny function. The Glibc.modulemap contains absolute include
# pathes like so:
#      header "/usr/include/aarch64-linux-gnu/sys/ioctl.h"
# This thing creates a new directory:
#   ${ARCH_NAME}-swift.xctoolchain/usr/lib/swift/linux/aarch64/private_includes
# and for each header in the modmap it creates a shim header which includes
# a relative path, like:
#   ${ARCH_NAME}-swift.xctoolchain/usr/lib/swift/linux/aarch64/private_includes/aarch64-linux-gnu_sys_ioctl.h
# which includes:
#   #include <aarch64-linux-gnu/sys/ioctl.h>
function fix_glibc_modulemap() {
    local glc_mm
    local tmp
    local inc_dir

    glc_mm="$1"
    if ! test -f "$glc_mm"; then
        echo "Missing: $glc_mm"
        exit 42
    fi

    tmp=$(mktemp "$glc_mm"_orig_XXXXXX)
    inc_dir="$(dirname "$glc_mm")/private_includes"
    cat "$glc_mm" >>"$tmp"
    echo -n >"$glc_mm"
    rm -rf "$inc_dir"
    mkdir "$inc_dir"
    cat "$tmp" | while IFS='' read line; do
        # hh: apparently the modmap started w/ two slashes? ///usr/local/
        # if [[ "$line" =~ ^(\ *header\ )\"\/\/\/usr\/include\/(${TC_TARGET}\/)?([^\"]+)\" ]]; then
        if [[ "$line" =~ ^(\ *header\ )\"\/usr\/include\/(${TC_TARGET}\/)?([^\"]+)\" ]]; then
            local orig_inc
            local rel_repl_inc
            local repl_inc

            orig_inc="${BASH_REMATCH[3]}"
            rel_repl_inc="$(echo "$orig_inc" | tr / _)"
            repl_inc="$inc_dir/$rel_repl_inc"
            echo "${BASH_REMATCH[1]} \"$(basename "$inc_dir")/$rel_repl_inc\"" >>"$glc_mm"
            if [[ "$orig_inc" == "uuid/uuid.h" ]]; then
                # no idea why ;)
                echo "#include <linux/uuid.h>" >>"$repl_inc"
            else
                echo "#include <$orig_inc>" >>"$repl_inc"
            fi
            true
        else
            echo "$line" >>"$glc_mm"
        fi
    done
}


fix_glibc_modulemap $1
#!/bin/bash
set -e

# Configuration
SRC_ROOT="${SRC_ROOT:=$(pwd)}"
POKY_DIR="${POKY_DIR:=$SRC_ROOT/../poky}"

# Build Yocto Poky
cd $POKY_DIR
source oe-init-build-env
bitbake-layers add-layer $SRC_ROOT
# Customize build
CONF_FILE=./conf/local.conf
rm -rf $CONF_FILE
echo "# Swift for Yocto" >> $CONF_FILE
echo 'MACHINE="beaglebone-yocto"' >> $CONF_FILE
echo 'SSTATE_MIRRORS ?= "file://.* http://sstate.yoctoproject.org/all/PATH;downloadfilename=PATH"' >> $CONF_FILE

# build Swift
bitbake swift-stdlib
bitbake libfoundation
bitbake swift-hello-world

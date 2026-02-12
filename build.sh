#!/bin/bash

#
# Normally this is called as '. ./ale-init-build-env builddir'
#
# This works in most shells (not dash), but not all of them pass arg1 when
# being sourced. To workaround the shell limitation use "set arg1" prior
# to sourcing this script.
#
if [ -n "$BASH_SOURCE" ]; then
   THIS_SCRIPT_DIR="$(readlink -f $(dirname $BASH_SOURCE))"
elif [ -n "$ZSH_NAME" ]; then
   THIS_SCRIPT_DIR="$(readlink -f $(dirname $0))"
else
   THIS_SCRIPT_DIR="$(pwd)"
fi

BUILD_DIR="${THIS_SCRIPT_DIR}/build"
DEPLOY_DIR="${THIS_SCRIPT_DIR}/image"

source source/openembedded-core/oe-init-build-env $BUILD_DIR

LOCAL_CONF_CONTENT='
PACKAGE_CLASSES ?= "package_ipk"
USER_CLASSES ?= "buildstats" 
PATCHRESOLVE = "noop"

BB_DISKMON_DIRS ??= "\
    STOPTASKS,${TMPDIR},1G,100K \
    STOPTASKS,${DL_DIR},1G,100K \
    STOPTASKS,${SSTATE_DIR},1G,100K \
    STOPTASKS,/tmp,100M,100K \
    HALT,${TMPDIR},100M,1K \
    HALT,${DL_DIR},100M,1K \
    HALT,${SSTATE_DIR},100M,1K \
    HALT,/tmp,10M,1K"
    
PACKAGECONFIG:append:pn-qemu-system-native = " sdl"
PACKAGECONFIG:append:pn-nativesdk-qemu = " sdl"
CONF_VERSION = "2"   

MACHINE ?= "p3768-0000-p3767-0004" 

DISTRO_FEATURES:append = " opengl"

IMAGE_CLASSES += " image_types_tegra"
IMAGE_FSTYPES = "tegraflash"

EXTRA_IMAGE_FEATURES += "allow-empty-password" 
EXTRA_IMAGE_FEATURES += "empty-root-password" 
EXTRA_IMAGE_FEATURES += "allow-root-login"

IMAGE_FEATURES:append = " ssh-server-openssh"
IMAGE_INSTALL:append = " openssh openssh-sshd"
IMAGE_INSTALL:append = " pciutils ethtool iproute2 iputils linux-firmware dhcpcd"

TEGRA_PLUGIN_MANAGER_OVERLAYS:append = "tegra234-p3767-camera-p3768-vc_mipi-dual.dtbo tegra234-p3768-usb3-superspeed-lanes.dtbo"

BB_NUMBER_THREADS = "4"
PARALLEL_MAKE = "-j4"

DEPLOY_DIR_IMAGE = "${TOPDIR}/image/"

BBDEBUG = "yes"
'

BBLAYERS_CONF_CONTENT='
LCONF_VERSION = "7"

BBPATH = "${TOPDIR}"
BBFILES ?= ""

BBLAYERS ?= " \
  ${TOPDIR}/../source/meta-tegra \
  ${TOPDIR}/../source/openembedded-core/meta \
  ${TOPDIR}/../source/meta-openembedded/meta-oe \
  ${TOPDIR}/../source/meta-openembedded/meta-python \
  ${TOPDIR}/../source/meta-openembedded/meta-networking \
  ${TOPDIR}/../source/meta-openembedded/meta-filesystems \
  ${TOPDIR}/../source/meta-custom-bsp \
"
'
echo "${LOCAL_CONF_CONTENT}" > "${BUILD_DIR}/conf/local.conf"
echo "${BBLAYERS_CONF_CONTENT}" > "${BUILD_DIR}/conf/bblayers.conf"

bitbake core-image-full-cmdline

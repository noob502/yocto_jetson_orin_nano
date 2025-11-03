#!/bin/bash

BASE_DIR=$(pwd)
BUILD_DIR="${BASE_DIR}/build"
DEPLOY_DIR="${BASE_DIR}/image"

source source/openembedded-core/oe-init-build-env $BUILD_DIR

LOCAL_CONF_CONTENT='
PACKAGE_CLASSES ?= "package_ipk"
#PACKAGE_CLASSES ?= "package_ipk package_deb package_rpm"
EXTRA_IMAGE_FEATURES ?= "debug-tweaks" 
#EXTRA_IMAGE_FEATURES ?= "debug-tweaks deb-pkgs" 
#EXTRA_IMAGE_FEATURES ?= "debug-tweaks deb-pkgs package-management" 
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

MACHINE ?= "p3509-a02-p3767-0000"
#DISTRO = "surfacecontrol-os_development"
DISTRO = "surfaceos"

INHERIT += " rm_work"
DISTRO_FEATURES = " x11 opengl systemd"
VIRTUAL-RUNTIME_init_manager = "systemd"
DISTRO_FEATURES:append = " systemd docker seccomp"
DISTRO_FEATURES:append = " virtualization"
IMAGE_CLASSES += " image_types_tegra"
IMAGE_FSTYPES = "tegraflash"
TEGRAFLASH_INITRD_IMAGE = "tegra-initrd-flash-initramfs"

#inherit core-image extrausers

INHERIT:remove = "extrausers"
EXTRA_USERS_PARAMS = ""
EXTRA_USERS_PARAMS:pn-core-image-full-cmdline = ""
EXTRA_USERS_PARAMS:pn-tegra-espimage = ""
EXTRA_USERS_PARAMS:pn-tegra-initrd-flash-initramfs = ""

IMAGE_FEATURES += "package-management ssh-server-openssh"
#IMAGE_FEATURES_remove = "ssh-server-dropbear"
EXTRA_IMAGE_FEATURES += "allow-empty-password"
EXTRA_IMAGE_FEATURES += "empty-root-password"
EXTRA_IMAGE_FEATURES += "allow-root-login"

APT_ARGS = "-oDebug::pkgProblemResolver=true -oDebug::pkgDPkgPM=true"

#IMAGE_FSTYPES = "wic wic.bamp"

APPEND += " net.ifnames=0"

IMAGE_INSTALL:append = " cuda-driver tegra-libraries-cuda gcc python3 \
                        v4l-utils util-linux gstreamer1.0-plugins-nvvideo4linux2 \
                        apt dpkg sudo tegra-mmapi tegra-mmapi-dev \
                        i2c-tools bash dtc pkgconfig glibc libstdc++ libgcc \
                        python3-pyserial spidev-test spitools kernel-module-spidev \
                        lmsensors busybox kernel-devicetree cmake systemd \
                        systemd-conf openssh ethtool \
                        linux-firmware kernel-modules lshw pciutils \
                        packagegroup-core-buildessential \
                        vim htop docker \
                        p7zip yaml-cpp libyaml \
"
IMAGE_INSTALL:append = " nano networkmanager networkmanager-nmcli"

IMAGE_INSTALL:append = " grep findutils gawk"

IMAGE_INSTALL:append = " openssl libcap zlib busybox-udhcpc"

KERNEL_MODULE_AUTOLOAD += "spidev"
KERNEL_DEVICETREE_APPLY_OVERLAYS = "tegra234-p3767-0000-p3509-a02-hdr40.dtbo"

# Ensure bmap generation runs after do_image
#ROOTFS_POSTPROCESS_COMMAND += " do_generate_bmap; "


BB_NUMBER_THREADS = "4"
PARALLEL_MAKE = "-j4"

DEPLOY_DIR_IMAGE = "${TOPDIR}/image/"
#rm -rf "${TEMP_FLASH_DIR}"

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
  ${TOPDIR}/../source/meta-virtualization \
"
'

echo "${LOCAL_CONF_CONTENT}" > "${BUILD_DIR}/conf/local.conf"
echo "${BBLAYERS_CONF_CONTENT}" > "${BUILD_DIR}/conf/bblayers.conf"

bitbake -c clean virtual/kernel
bitbake virtual/kernel

bitbake -c cleansstate pleora sensorsoftware
bitbake pleora sensorsoftware

bitbake -c cleansstate dhclient-conf

bitbake -c cleansstate sensorconfig
bitbake sensorconfig

bitbake -c cleansstate core-image-full-cmdline
bitbake core-image-full-cmdline

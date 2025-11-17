FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

VC_MIPI_PATCHES = " \
    file://0001-Added-cropping-position-left-top-to-sensor-image-pro.patch \
    file://0002-Added-implementation-to-set-image-position-and-size-.patch \
    file://0003-Reduced-image-size-limitation-from-width-32-to-4-and.patch \
    file://0004-Added-controls-trigger_mode-io_mode-black_level-sing.patch \
    file://0005-Added-RAW8-grey-RAW10-y10-RAW12-y12-RAW14-y14-rggb8-.patch \
    file://0006-Added-VC-MIPI-Driver-sources-to-Makefile.patch \
    file://0007-Added-VC-MIPI-driver-to-Kconfig.patch \
    file://0008-Changed-Interrupt-Mask-for-csi4-to-emit-CRC-and-mult.patch \
    file://0009-Disable-VB2_BUF_STATE_REQUEUEING-in-vi5_fops.c.patch \
    file://0010-Fixed-compiler-error-for-nv_ar0234-and-nv_hawk_owl.patch \
    file://0011-Handler-function-ready_to_stream-introduced.patch \
    file://0012-Increased-tegra-channel-timeout.patch \
    file://0013-Infinite-timeout-for-Orin-and-Xavier.patch \
    file://0014-Modified-tegra194-p2888-0001-p2822-0000.dts-to-integ.patch \
    file://0015-Modified-tegra194-p3509-0000-a00.dtsi-to-integrate-V.patch \
    file://0016-Modified-tegra234-p3509-a02.dtsi-to-integrate-VC-MIP.patch \
    file://0017-Modified-tegra234-p3768-0000-a0.dts-to-integrate-VC-.patch \
    file://0018-Stability-patch.-work_struct-refactored-to-kthread.patch \
    file://0019-The-function-tegracam_v4l2subdev_register-did-not-lo.patch \
    file://0020-Add-vc-mipi-devicetrees.patch \
    file://0021-Add-vc-mipi-driver.patch \
"

USB_SUPER_MODE_PATCHES = " \
    file://0001-tegra234-soc-base.dtsi-enable-usb3-super-speed.patch \
"

SRC_URI:append = " \
    ${VC_MIPI_PATCHES} \
    ${USB_SUPER_MODE_PATCHES} \
"

FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

VC_MIPI_PATCHES = " \
    file://0001-Added-controls-trigger_mode-io_mode-black_level-sing.patch \
    file://0002-Added-cropping-position-left-top-to-sensor-image-pro.patch \
    file://0003-Added-implementation-to-set-image-position-and-size-.patch \
    file://0004-Added-RAW8-grey-RAW10-y10-RAW12-y12-RAW14-y14-rggb8-.patch \
    file://0005-Added-VC-MIPI-Driver-sources-to-nvidia-oot-Makefile.patch \
    file://0006-Handler-function-ready_to_stream-introduced.patch \
    file://0007-Increased-tegra-channel-timeout.patch \
    file://0008-Infinite-timeout-for-Orin.patch \
    file://0009-Modified-overlay-Makefile-to-integrate-VC-MIPI-Drive.patch \
    file://0010-Reduced-image-size-limitation-from-width-32-to-4-and.patch \
    file://0011-Suppress-discarding-frame-warning.patch \
    file://0012-tegra234-p3767-camera-p3768-vc_mipi-dual.dts-add-dev.patch \
    file://0013-vc_mipi-add-drivers.patch \
"

USB_SUPER_SPEED_PATCHES = " \
    file://0001-tegra234.dtsi-enable-usb3-super-speed.patch \
"

SRC_URI:append = " \
    ${VC_MIPI_PATCHES} \
    ${USB_SUPER_SPEED_PATCHES} \
"

TEGRA_OOT_CAMERA_DRIVERS += "nv-kernel-module-vc-mipi-camera nv-kernel-module-vc-mipi-core"

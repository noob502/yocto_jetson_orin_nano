FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

SRC_URI:append = " \
    file://0001-Handler-function-ready_to_stream-introduced.patch \
"

KERNEL_MODULE_AUTOLOAD = "vc_mipi_camera"

ALLOW_EMPTY:kernel-devicetree = "1"

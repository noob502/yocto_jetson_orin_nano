FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

SRC_URI:append = " \
    file://0013-tegra234-p3768-usb3-superspeed-lanes-add-dts.patch \
"

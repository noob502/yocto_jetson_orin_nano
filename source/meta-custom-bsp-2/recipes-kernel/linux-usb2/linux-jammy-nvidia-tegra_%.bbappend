FILESEXTRAPATHS:prepend := "${THISDIR}/linux-tegra:"

SRC_URI += "file://usb2-0-host.dtso"

DEPENDS:append = " dtc-native"

do_compile:append() {
    dtc -@ -I dts -O dtb -o ${B}/usb2-0-host.dtbo ${WORKDIR}/usb2-0-host.dtso
}

do_deploy:append() {
    install -m 0644 ${B}/usb2-0-host.dtbo ${DEPLOYDIR}/
}

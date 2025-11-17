FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

SRC_URI:append = " \
    file://0001-tegra234-mb1-bct-gpio-p3767-custom-a03.dtsi-add-cust.patch \
    file://0002-tegra234-mb1-bct-padvoltage-p3767-custom-a03.dtsi-ad.patch \
    file://0003-tegra234-mb1-bct-pinmux-p3767-custom-a03.dtsi-add-cu.patch \
"

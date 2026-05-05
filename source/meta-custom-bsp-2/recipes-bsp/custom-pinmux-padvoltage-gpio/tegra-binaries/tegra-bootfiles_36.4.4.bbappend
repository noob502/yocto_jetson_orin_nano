FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

SRC_URI:append:custom-jon-bb0 = " \
    file://tegra234-custom-jon-bb0-gpio-default.dtsi \
    file://tegra234-custom-jon-bb0-padvoltage-default.dtsi \
    file://tegra234-custom-jon-bb0-pinmux.dtsi \
"

CUSTOM_DTSI_DIR := "${THISDIR}/files"

do_install:append:custom-jon-bb0() {
    install -m 0644 ${CUSTOM_DTSI_DIR}/tegra234-custom-jon-bb0-gpio-default.dtsi \
        ${D}${datadir}/tegraflash/
    install -m 0644 ${CUSTOM_DTSI_DIR}/tegra234-custom-jon-bb0-padvoltage-default.dtsi \
        ${D}${datadir}/tegraflash/
    install -m 0644 ${CUSTOM_DTSI_DIR}/tegra234-custom-jon-bb0-pinmux.dtsi \
        ${D}${datadir}/tegraflash/
}

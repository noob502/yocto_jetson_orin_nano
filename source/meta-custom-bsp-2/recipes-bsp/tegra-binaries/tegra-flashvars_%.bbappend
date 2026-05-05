FILESEXTRAPATHS:prepend := "${THISDIR}/tegra-flashvars:"

SRC_URI:append:custom-jon8-bb0-4 = " file://flashvars"

do_install:append:custom-jon8-bb0-4() {
    install -m 0644 ${WORKDIR}/flashvars ${D}${datadir}/tegraflash/flashvars
}

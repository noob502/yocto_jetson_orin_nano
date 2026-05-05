DTS_OVERRIDE := "${@os.path.join(os.path.dirname(d.getVar('FILE')), 'files', 'tegra234-mb2-bct-misc-p3767-0000.dts')}"

do_install:append() {
    install -d ${D}${datadir}/tegraflash

    install -m 0644 \
        ${DTS_OVERRIDE} \
        ${D}${datadir}/tegraflash/tegra234-mb2-bct-misc-p3767-0000.dts
}


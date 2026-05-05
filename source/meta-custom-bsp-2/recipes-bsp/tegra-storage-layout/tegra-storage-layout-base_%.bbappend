FILESEXTRAPATHS:prepend := "${THISDIR}/tegra-storage-layout-base:"

SRC_URI += " file://flash_l4t_t234_nvme_rootfs_ab.xml"

CUSTOM_PART_XML := "${THISDIR}/tegra-storage-layout-base/flash_l4t_t234_nvme_rootfs_ab.xml"

do_install:append() {
    install -d ${D}${datadir}/l4t-storage-layout
    install -m 0644 ${CUSTOM_PART_XML} \
        ${D}${datadir}/l4t-storage-layout/flash_l4t_t234_nvme_rootfs_ab.xml
}

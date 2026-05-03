FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

LIC_FILES_CHKSUM:remove = "file://../edk2-nvidia/LICENSE;md5=52d8683e3d65a77ef84cc863e3f24f25"

SRC_URI:append = " \
    file://0001-L4TLauncher-force-to-direct-boot.patch;patchdir=../edk2-nvidia \
"
inherit dos2unix

do_convert_crlf_to_lf () {
    find ${S}/../edk2-nvidia -type f -exec dos2unix {} \;
}

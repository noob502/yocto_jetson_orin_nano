require recipes-bsp/tegra-binaries/tegra-debian-libraries-common.inc

LICENSE = "CLOSED"
LIC_FILES_CHKSUM = ""

L4T_VERSION = "12.6"
L4T_BSP_DEB_DEFAULT_VERSION = "34622040.0"
L4T_DEB_SOCNAME = "common"

MAINSUM = "72c11a87ce3f741d2218957eb842d8a6061ca650ae0defc367ed322fffc45c46"

TEGRA_LIBRARIES_TO_INSTALL = "\
    libcudadebugger.so.1 \
"

do_install() {
    install_libraries
    ln -sf libcudadebugger.so.1 ${D}${libdir}/libcudadebugger.so
}

FILES_SOLIBSDEV = ""
SOLIBS = ".so*"
RPROVIDES:${PN} += "libcudadebugger.so()(64bit)"
RRECOMMENDS:${PN} = "tegra-libraries-cuda"

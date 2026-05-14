# Common carrier-board settings 

MACHINEOVERRIDES .= ":custom-jon-bb0"

TEGRA_PLUGIN_MANAGER_OVERLAYS:append = " tegra234-p3767-camera-p3768-vc_mipi-dual.dtbo"

USE_REDUNDANT_FLASH_LAYOUT_DEFAULT = "1"
ROOTFSPART_SIZE_REDUNDANT = "10737418240"

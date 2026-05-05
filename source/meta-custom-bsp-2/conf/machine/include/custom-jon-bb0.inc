# Common carrier-board settings 

MACHINEOVERRIDES:prepend = "custom-jon-bb0:"

# Common custom MB1 config for this carrier board
TEGRA_FLASHVAR_PINMUX_CONFIG ?= "tegra234-custom-jon-bb0-pinmux.dtsi"
TEGRA_FLASHVAR_PMC_CONFIG ?= "tegra234-custom-jon-bb0-padvoltage-default.dtsi"

FILESEXTRAPATHS:prepend := "${THISDIR}/${BPN}:"

SRC_URI:append = " \
    file://0001-Handler-function-ready_to_stream-introduced.patch \
"

ALLOW_EMPTY:kernel-devicetree = "1"

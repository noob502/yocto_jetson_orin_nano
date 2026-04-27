SUMMARY = "Import persistent secure boot assets for Jetson Orin Stage 1"
LICENSE = "CLOSED"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit nopackages deploy

SECUREBOOT_ASSETS_DIR ?= "/yocto/secure-assets/${MACHINE}"

do_compile() {
    install -d ${B}/pkc
    install -d ${B}/oem

    test -f ${SECUREBOOT_ASSETS_DIR}/pkc_rsa3072.pem || bbfatal "Missing pkc_rsa3072.pem"
    test -f ${SECUREBOOT_ASSETS_DIR}/pkc_rsa3072_pub.pem || bbfatal "Missing pkc_rsa3072_pub.pem"
    test -f ${SECUREBOOT_ASSETS_DIR}/sbk-32.key || bbfatal "Missing sbk-32.key"
    test -f ${SECUREBOOT_ASSETS_DIR}/oemk1-32.key || bbfatal "Missing oemk1-32.key"

    install -m 0600 ${SECUREBOOT_ASSETS_DIR}/pkc_rsa3072.pem ${B}/pkc/
    install -m 0644 ${SECUREBOOT_ASSETS_DIR}/pkc_rsa3072_pub.pem ${B}/pkc/
    install -m 0600 ${SECUREBOOT_ASSETS_DIR}/sbk-32.key ${B}/pkc/
    install -m 0600 ${SECUREBOOT_ASSETS_DIR}/oemk1-32.key ${B}/oem/

    sha256sum ${B}/pkc/pkc_rsa3072_pub.pem > ${B}/pkc/pkc_rsa3072_pub.pem.sha256

    tr -d ' \r\n' < ${B}/pkc/sbk-32.key | sed 's/0x//g' > ${B}/pkc/sbk.key.hex
    tr -d ' \r\n' < ${B}/oem/oemk1-32.key | sed 's/0x//g' > ${B}/oem/oemk1.key.hex
}

do_deploy() {
    out="${DEPLOYDIR}/secureboot"
    install -d ${out}/pkc
    install -d ${out}/oem

    install -m 0600 ${B}/pkc/pkc_rsa3072.pem ${out}/pkc/
    install -m 0644 ${B}/pkc/pkc_rsa3072_pub.pem ${out}/pkc/
    install -m 0644 ${B}/pkc/pkc_rsa3072_pub.pem.sha256 ${out}/pkc/
    install -m 0600 ${B}/pkc/sbk-32.key ${out}/pkc/
    install -m 0600 ${B}/pkc/sbk.key.hex ${out}/pkc/

    install -m 0600 ${B}/oem/oemk1-32.key ${out}/oem/
    install -m 0600 ${B}/oem/oemk1.key.hex ${out}/oem/

    cat > ${out}/README.txt <<'EOF'
Stage 1 secure boot assets imported from persistent host storage.
Contents:
- secureboot/pkc/pkc_rsa3072.pem
- secureboot/pkc/pkc_rsa3072_pub.pem
- secureboot/pkc/sbk-32.key
- secureboot/pkc/sbk.key.hex
- secureboot/oem/oemk1-32.key
- secureboot/oem/oemk1.key.hex
EOF
}

addtask deploy after do_compile

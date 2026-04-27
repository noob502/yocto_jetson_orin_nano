SUMMARY = "Create Stage 2 UEFI Secure Boot bundle and helper scripts"
LICENSE = "CLOSED"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit nopackages deploy

DEPENDS = "os-secureboot-uefi-assets"

do_compile[depends] += "os-secureboot-uefi-assets:do_deploy"

L4T_BOARD ?= "os-jon4-bb0-4"
L4T_EXTERNAL_DEVICE ?= "nvme0n1p1"
L4T_EXTERNAL_XML ?= "bootloader/generic/cfg/flash_l4t_t234_nvme_rootfs_ab.xml"
L4T_QSPI_XML ?= "bootloader/generic/cfg/flash_t234_qspi.xml"

do_compile() {
    DEPLOY="${DEPLOY_DIR_IMAGE}"

    install -d ${B}/bundle
    install -d ${B}/bundle/secureboot
    install -d ${B}/bundle/secureboot/uefi

    test -d ${DEPLOY}/secureboot/uefi || bbfatal "Missing ${DEPLOY}/secureboot/uefi"

    cp -a ${DEPLOY}/secureboot/uefi/* ${B}/bundle/secureboot/uefi/

    cat > ${B}/bundle/stage2-flash-with-uefi-sb.sh <<EOF
#!/bin/sh
set -eu

DIR="\$(CDPATH= cd -- "\$(dirname -- "\$0")" && pwd)"
L4T="\${DIR}/Linux_for_Tegra"
UEFI_KEYS="\${DIR}/secureboot/uefi/uefi_keys.conf"
PKC="\${DIR}/secureboot/pkc/pkc_rsa3072.pem"
SBK="\${DIR}/secureboot/pkc/sbk-32.key"

cd "\${L4T}"

sudo ./tools/kernel_flash/l4t_initrd_flash.sh \\
  --external-device ${L4T_EXTERNAL_DEVICE} \\
  --uefi-keys "\${UEFI_KEYS}" \\
  -u "\${PKC}" \\
  -v "\${SBK}" \\
  -p "-c ./${L4T_QSPI_XML}" \\
  -c ./${L4T_EXTERNAL_XML} \\
  --showlogs \\
  --network usb0 \\
  ${L4T_BOARD} external
EOF

    cat > ${B}/bundle/stage2-dev-enroll-on-target.sh <<'EOF'
#!/bin/sh
set -eu

echo "Run this on the target from Ubuntu userspace, not on the host."
echo "Expected files in /uefi_keys: PK.auth KEK.auth db.auth"
echo
echo "Enroll KEK and db first:"
echo "  sudo efi-updatevar -f /uefi_keys/db.auth db"
echo "  sudo efi-updatevar -f /uefi_keys/KEK.auth KEK"
echo
echo "Then enroll PK last:"
echo "  sudo efi-updatevar -f /uefi_keys/PK.auth PK"
echo
echo "Check status:"
echo "  efivar -n 8be4df61-93ca-11d2-aa0d-00e098032b8c-SecureBoot"
EOF

    cat > ${B}/bundle/stage2-dev-disable-on-target.sh <<'EOF'
#!/bin/sh
set -eu

echo "Run this on the target only, development use only:"
echo "  sudo efi-updatevar -f /uefi_keys/noPK.auth PK"
EOF

    cat > ${B}/bundle/README.txt <<'EOF'
Stage 2 UEFI Secure Boot bundle.

Primary production-oriented flow:
- Use stage2-flash-with-uefi-sb.sh
- This enables UEFI Secure Boot during flashing with --uefi-keys

Development-only runtime flow:
- Copy *.auth to target /uefi_keys
- Enroll db and KEK first, then PK last with efi-updatevar
EOF

    chmod 0755 ${B}/bundle/stage2-flash-with-uefi-sb.sh
    chmod 0755 ${B}/bundle/stage2-dev-enroll-on-target.sh
    chmod 0755 ${B}/bundle/stage2-dev-disable-on-target.sh
}

do_deploy() {
    out="${DEPLOYDIR}/secureboot-stage2-bundle"
    rm -rf "${out}"
    install -d "${out}"
    cp -a ${B}/bundle/* "${out}/"
}

addtask deploy after do_compile

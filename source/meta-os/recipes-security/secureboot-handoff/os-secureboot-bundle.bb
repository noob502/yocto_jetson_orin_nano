SUMMARY = "Create Stage 1 secure boot bundle and helper scripts"
LICENSE = "CLOSED"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit nopackages deploy

DEPENDS = "lft-minimal os-secureboot-assets os-secureboot-fusexml"

do_compile[depends] += "lft-minimal:do_deploy"
do_compile[depends] += "os-secureboot-assets:do_deploy"
do_compile[depends] += "os-secureboot-fusexml:do_deploy"
do_compile[depends] += "virtual/kernel:do_deploy"
do_compile[depends] += "core-image-full-cmdline:do_image_complete"

L4T_BOARD ?= "os-jon4-bb0-4"
L4T_EXTERNAL_DEVICE ?= "nvme0n1p1"
L4T_EXTERNAL_XML ?= "bootloader/generic/cfg/flash_l4t_t234_nvme_rootfs_ab.xml"
L4T_QSPI_XML ?= "bootloader/generic/cfg/flash_t234_qspi.xml"
L4T_DTB_FILE ?= "tegra234-p3768-0000+p3767-0004-nv-super.dtb"

do_compile() {
    DEPLOY="${DEPLOY_DIR_IMAGE}"

    install -d ${B}/bundle
    install -d ${B}/bundle/secureboot/pkc
    install -d ${B}/bundle/secureboot/oem
    install -d ${B}/bundle/secureboot/fusexml

    test -d ${DEPLOY}/lft-minimal/Linux_for_Tegra || bbfatal "Missing ${DEPLOY}/lft-minimal/Linux_for_Tegra"
    test -d ${DEPLOY}/secureboot/pkc || bbfatal "Missing ${DEPLOY}/secureboot/pkc"
    test -d ${DEPLOY}/secureboot/oem || bbfatal "Missing ${DEPLOY}/secureboot/oem"
    test -d ${DEPLOY}/secureboot/fusexml || bbfatal "Missing ${DEPLOY}/secureboot/fusexml"

    cp -a ${DEPLOY}/lft-minimal/Linux_for_Tegra ${B}/bundle/Linux_for_Tegra
    cp -a ${DEPLOY}/secureboot/pkc/* ${B}/bundle/secureboot/pkc/
    cp -a ${DEPLOY}/secureboot/oem/* ${B}/bundle/secureboot/oem/
    cp -a ${DEPLOY}/secureboot/fusexml/* ${B}/bundle/secureboot/fusexml/

    install -d ${B}/bundle/Linux_for_Tegra/kernel
    install -d ${B}/bundle/Linux_for_Tegra/kernel/dtb

    # Pick a kernel image from the deploy area
    KIMG=""
    for f in \
        ${DEPLOY}/Image \
        ${DEPLOY}/Image-${MACHINE}.bin \
        ${DEPLOY}/Image-*.bin
    do
        [ -f "$f" ] || continue
        KIMG="$f"
        break
    done

    [ -n "$KIMG" ] || bbfatal "Missing kernel image in ${DEPLOY}"
    install -m 0644 "$KIMG" ${B}/bundle/Linux_for_Tegra/kernel/Image

    # Copy the main board DTB explicitly after image artifacts are complete
    DTB_SRC=""

    if [ -f ${DEPLOY}/devicetree/${L4T_DTB_FILE} ]; then
        DTB_SRC="${DEPLOY}/devicetree/${L4T_DTB_FILE}"
    elif [ -f ${DEPLOY}/${L4T_DTB_FILE} ]; then
        DTB_SRC="${DEPLOY}/${L4T_DTB_FILE}"
    fi

    [ -n "${DTB_SRC}" ] || bbfatal "Could not find main board DTB ${L4T_DTB_FILE} in ${DEPLOY}"
    install -m 0644 "${DTB_SRC}" ${B}/bundle/Linux_for_Tegra/kernel/dtb/

    # Copy any deploy-time dtbo helpers if present
    if [ -d ${DEPLOY}/devicetree ]; then
        for f in ${DEPLOY}/devicetree/*.dtbo; do
            [ -e "$f" ] || continue
            install -m 0644 "$f" ${B}/bundle/Linux_for_Tegra/kernel/dtb/
        done
    fi

    # Copy any top-level dtbo helpers from deploy root
    for f in ${DEPLOY}/*.dtbo; do
        [ -e "$f" ] || continue
        install -m 0644 "$f" ${B}/bundle/Linux_for_Tegra/kernel/dtb/
    done

    chmod 0755 ${B}/bundle/Linux_for_Tegra/flash.sh || true
    chmod 0755 ${B}/bundle/Linux_for_Tegra/tools/kernel_flash/l4t_initrd_flash.sh || true
    chmod 0755 ${B}/bundle/Linux_for_Tegra/tools/kernel_flash/l4t_initrd_flash_internal.sh || true
    chmod 0755 ${B}/bundle/Linux_for_Tegra/bootloader/tegrarcm_v2 || true
    chmod 0755 ${B}/bundle/Linux_for_Tegra/bootloader/tegradevflash_v2 || true
    chmod 0755 ${B}/bundle/Linux_for_Tegra/bootloader/tegraparser_v2 || true
    chmod 0755 ${B}/bundle/Linux_for_Tegra/bootloader/tegrabct_v2 || true
    chmod 0755 ${B}/bundle/Linux_for_Tegra/bootloader/tegrahost_v2 || true
    chmod 0755 ${B}/bundle/Linux_for_Tegra/bootloader/tegrasign_v2 || true
    chmod 0755 ${B}/bundle/Linux_for_Tegra/bootloader/tegrasign_v3.py || true
    chmod 0755 ${B}/bundle/Linux_for_Tegra/bootloader/chkbdinfo || true
    chmod -R a+rX ${B}/bundle/Linux_for_Tegra/bootloader || true
    chmod 0755 ${B}/bundle/Linux_for_Tegra/bootloader/tegraopenssl || true

    cat > ${B}/bundle/stage1-sign.sh <<EOF
#!/bin/sh
set -eu

DIR="\$(CDPATH= cd -- "\$(dirname -- "\$0")" && pwd)"
L4T="\${DIR}/Linux_for_Tegra"
PKC="\${DIR}/secureboot/pkc/pkc_rsa3072.pem"
SBK="\${DIR}/secureboot/pkc/sbk-32.key"

cd "\${L4T}"

sudo ./tools/kernel_flash/l4t_initrd_flash.sh \\
  --no-flash \\
  --external-device ${L4T_EXTERNAL_DEVICE} \\
  -u "\${PKC}" \\
  -v "\${SBK}" \\
  -p "-c ./${L4T_QSPI_XML}" \\
  -c ./${L4T_EXTERNAL_XML} \\
  --showlogs \\
  --network usb0 \\
  ${L4T_BOARD} external
EOF

    cat > ${B}/bundle/stage1-flash.sh <<EOF
#!/bin/sh
set -eu

DIR="\$(CDPATH= cd -- "\$(dirname -- "\$0")" && pwd)"
L4T="\${DIR}/Linux_for_Tegra"

cd "\${L4T}"

sudo ./tools/kernel_flash/l4t_initrd_flash.sh \\
  --flash-only \\
  --external-device ${L4T_EXTERNAL_DEVICE} \\
  -p "-c ./${L4T_QSPI_XML}" \\
  -c ./${L4T_EXTERNAL_XML} \\
  --showlogs \\
  --network usb0 \\
  ${L4T_BOARD} external
EOF

    cat > ${B}/bundle/stage1-fusecheck.sh <<EOF
#!/bin/sh
set -eu

DIR="\$(CDPATH= cd -- "\$(dirname -- "\$0")" && pwd)"
L4T="\${DIR}/Linux_for_Tegra"
FUSEXML="\${DIR}/secureboot/fusexml/fuse.xml"

echo "Checking Stage 1 fuse prerequisites..."
test -f "\${FUSEXML}" || { echo "Missing fuse.xml"; exit 1; }

if [ -d "\${L4T}/tools/flashtools/fuseburn" ]; then
    echo "FSKP tool directory found:"
    echo "  \${L4T}/tools/flashtools/fuseburn"
else
    echo "FSKP tool directory not found."
    echo "NVIDIA R36 uses Factory Secure Key Provisioning instead of odmfuse.sh."
    echo "Extract fskp_partner_t234.tbz2 into the Linux_for_Tegra environment first."
    exit 2
fi

echo "fuse.xml present:"
echo "  \${FUSEXML}"
echo "Fuse check passed."
EOF

    cat > ${B}/bundle/stage1-fskp-burn.sh <<EOF
#!/bin/sh
set -eu

DIR="\$(CDPATH= cd -- "\$(dirname -- "\$0")" && pwd)"
L4T="\${DIR}/Linux_for_Tegra"
FUSEXML="\${DIR}/secureboot/fusexml/fuse.xml"
FSKP_DIR="\${L4T}/tools/flashtools/fuseburn"

if [ ! -d "\${FSKP_DIR}" ]; then
    echo "Missing FSKP tool directory:"
    echo "  \${FSKP_DIR}"
    echo "Extract fskp_partner_t234.tbz2 into Linux_for_Tegra first."
    exit 2
fi

echo "FSKP environment detected."
echo "Use NVIDIA's FSKP fuse burn flow with:"
echo "  chip id: 0x23"
echo "  fuse file: \${FUSEXML}"
echo
echo "This script intentionally stops here."
echo "Reason: the exact FSKP command depends on your NVIDIA partner package contents"
echo "and provisioning files."
exit 3
EOF

    chmod 0755 ${B}/bundle/stage1-sign.sh
    chmod 0755 ${B}/bundle/stage1-flash.sh
    chmod 0755 ${B}/bundle/stage1-fusecheck.sh
    chmod 0755 ${B}/bundle/stage1-fskp-burn.sh

    cat > ${B}/bundle/README.txt <<EOF
Stage 1 secure boot private bundle.

Generated files:
- stage1-sign.sh
- stage1-flash.sh
- stage1-fusecheck.sh
- stage1-fskp-burn.sh

Execution order:
1. Run stage1-sign.sh
2. Run stage1-fusecheck.sh
3. Prepare NVIDIA FSKP tooling
4. Run FSKP fuse burn using the generated fuse.xml
5. Run stage1-flash.sh
EOF
}

do_deploy() {
    out="${DEPLOYDIR}/secureboot-stage1-bundle"
    rm -rf "${out}"
    install -d "${out}"
    cp -a ${B}/bundle/* "${out}/"
}

addtask deploy after do_compile

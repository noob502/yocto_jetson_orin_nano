SUMMARY = "Generate Jetson Orin Stage 1 fuse.xml"
LICENSE = "CLOSED"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit nopackages deploy python3native

DEPENDS = "python3-native python3-pyyaml-native lft-minimal os-secureboot-assets"

do_compile[depends] += "os-secureboot-assets:do_deploy"
do_compile[depends] += "lft-minimal:do_deploy"

SECUREBOOT_BOOT_SECURITY_INFO ?= "0x209"

do_compile() {
    install -d ${B}

    PKC_DIR="${TOPDIR}/image/secureboot/pkc"
    OEM_DIR="${TOPDIR}/image/secureboot/oem"
    L4T_DIR="${TOPDIR}/image/lft-minimal/Linux_for_Tegra"
    TEGRASIGN="${L4T_DIR}/bootloader/tegrasign_v3.py"

    test -f "${PKC_DIR}/pkc_rsa3072.pem" || bbfatal "Missing pkc_rsa3072.pem"
    test -f "${PKC_DIR}/sbk.key.hex" || bbfatal "Missing sbk.key.hex"
    test -f "${OEM_DIR}/oemk1.key.hex" || bbfatal "Missing oemk1.key.hex"
    test -f "${TEGRASIGN}" || bbfatal "Missing tegrasign_v3.py"

    cd ${B}

    PYTHON_BIN="$(command -v python3 || true)"
    [ -n "${PYTHON_BIN}" ] || bbfatal "python3 not found in task environment"

    PY_SITEPACKAGES="$(echo ${RECIPE_SYSROOT_NATIVE}/usr/lib/python3*/site-packages)"
    export PYTHONPATH="${PY_SITEPACKAGES}:${PYTHONPATH}"

    "${PYTHON_BIN}" "${TEGRASIGN}" \
        --pubkeyhash pkc_rsa3072.pubkey pkc_rsa3072.hash \
        --key "${PKC_DIR}/pkc_rsa3072.pem"

    test -f ${B}/pkc_rsa3072.hash || bbfatal "tegrasign did not generate pkc_rsa3072.hash"
    test -f ${B}/pkc_rsa3072.pubkey || bbfatal "tegrasign did not generate pkc_rsa3072.pubkey"

    PUBHASH=$(od -An -tx1 -v ${B}/pkc_rsa3072.hash | tr -d ' \n')
    SBKHEX=$(tr -d '\r\n' < "${PKC_DIR}/sbk.key.hex")
    OEMK1HEX=$(tr -d '\r\n' < "${OEM_DIR}/oemk1.key.hex")

    cat > ${B}/fuse.xml <<EOF
<genericfuse MagicId="0x45535546" version="1.0.0">
    <fuse name="PublicKeyHash" size="64" value="0x${PUBHASH}"/>
    <fuse name="SecureBootKey" size="32" value="0x${SBKHEX}"/>
    <fuse name="OemK1" size="32" value="0x${OEMK1HEX}"/>
    <fuse name="BootSecurityInfo" size="4" value="${SECUREBOOT_BOOT_SECURITY_INFO}"/>
    <fuse name="SecurityMode" size="4" value="0x1"/>
</genericfuse>
EOF

    cat > ${B}/README.txt <<EOF
Generated Stage 1 fuse.xml for Jetson Orin.
Inputs:
- PublicKeyHash from pkc_rsa3072.pem
- SecureBootKey from sbk-32.key
- OemK1 from oemk1-32.key
- BootSecurityInfo=${SECUREBOOT_BOOT_SECURITY_INFO}

WARNING:
- SecurityMode must be the last fuse
- Burning is irreversible
- Validate BootSecurityInfo against your final Orin security mode before actual fuse burn
EOF
}

do_deploy() {
    out="${DEPLOYDIR}/secureboot/fusexml"
    install -d "$out"

    install -m 0644 ${B}/fuse.xml "$out/"
    install -m 0644 ${B}/pkc_rsa3072.hash "$out/"
    install -m 0644 ${B}/pkc_rsa3072.pubkey "$out/"
    install -m 0644 ${B}/README.txt "$out/"
}

addtask deploy after do_compile

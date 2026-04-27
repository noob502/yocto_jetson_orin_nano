SUMMARY = "Import stable UEFI Secure Boot keys and generate ESL/auth/config artifacts"
LICENSE = "CLOSED"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit nopackages deploy

DEPENDS = "openssl-native util-linux-native"

SECURE_ASSETS_DIR ?= "/yocto/secure-assets/${MACHINE}/uefi"
UEFI_DEPLOY_DIR = "${DEPLOYDIR}/secureboot/uefi"

do_compile() {
    install -d ${B}

    for f in PK.key PK.crt KEK.key KEK.crt db.key db.crt; do
        if [ ! -f "${SECURE_ASSETS_DIR}/$f" ]; then
            bbfatal "Missing ${SECURE_ASSETS_DIR}/$f"
        fi
        install -m 0600 "${SECURE_ASSETS_DIR}/$f" ${B}/$f || true
    done

    chmod 0644 ${B}/*.crt

    # Flash-time config for NVIDIA --uefi-keys flow
    cat > ${B}/uefi_keys.conf <<'EOF'
    UEFI_DB_1_KEY_FILE="db.key";
    UEFI_DB_1_CERT_FILE="db.crt";
    EOF

UEFI_DB_1_KEY_FILE="db.key";
UEFI_DB_1_CERT_FILE="db.crt";

UEFI_DEFAULT_PK_ESL="PK.esl";
UEFI_DEFAULT_KEK_ESL_0="KEK.esl";

UEFI_DEFAULT_DB_ESL_0="db.esl";
EOF

    cat > ${B}/README.txt <<'EOF'
UEFI Secure Boot artifacts for Jetson Stage 2.

Contents:
- PK/KEK/db key pairs and certs
- PK.esl / KEK.esl / db.esl
- PK.auth / KEK.auth / db.auth
- noPK.auth
- uefi_keys.conf for NVIDIA flashing-time UEFI Secure Boot enablement

Notes:
- Keep *.key private
- Use --uefi-keys uefi_keys.conf during flash for production-oriented flow
- Runtime efi-updatevar enrollment is development-oriented
EOF
}

do_deploy() {
    out="${UEFI_DEPLOY_DIR}"
    rm -rf "${out}"
    install -d "${out}"

    # private
    install -m 0600 ${B}/PK.key  "${out}/"
    install -m 0600 ${B}/KEK.key "${out}/"
    install -m 0600 ${B}/db.key  "${out}/"

    # public and generated
    for f in \
        PK.crt KEK.crt db.crt \
        uefi_keys.conf README.txt
    do
        install -m 0644 ${B}/$f "${out}/"
    done
}

addtask deploy after do_compile

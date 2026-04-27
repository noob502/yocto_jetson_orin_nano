# os-secureboot-public.bbclass
#
# Exports ONLY public Secure Boot material to deploy:
#   PK.crt, KEK.crt, db.crt, uefi_keys.conf
# And blocks private keys from being copied into handoff output.

python os_secureboot_export_public () {
    import os, glob, shutil

    deploy = d.getVar("DEPLOY_DIR_IMAGE")
    outdir = os.path.join(deploy, "secureboot-public")
    os.makedirs(outdir, exist_ok=True)

    # Where your secureboot provisioning content ends up.
    # Adjust this if your project uses a different path.
    # You previously showed: build/image/secureboot/provisioning/uefi-keys/
    prov_candidates = [
        os.path.join(deploy, "secureboot", "provisioning", "uefi-keys"),
        os.path.join(deploy, "secureboot", "provisioning"),
    ]

    provdir = None
    for c in prov_candidates:
        if os.path.isdir(c):
            provdir = c
            break

    if not provdir:
        bb.note("No provisioning dir found under DEPLOY_DIR_IMAGE; skipping public export.")
        return

    # Copy only public certs and config
    allowed = ["PK.crt", "KEK.crt", "db.crt", "uefi_keys.conf"]
    for name in allowed:
        src = os.path.join(provdir, name)
        if os.path.exists(src):
            shutil.copy2(src, outdir)

    # Also allow certs stored under nested uefi-keys/
    nested = os.path.join(provdir, "uefi-keys")
    if os.path.isdir(nested):
        for name in ["PK.crt", "KEK.crt", "db.crt"]:
            src = os.path.join(nested, name)
            if os.path.exists(src):
                shutil.copy2(src, outdir)

    bb.note(f"Exported public Secure Boot material to: {outdir}")
}

python os_secureboot_assert_no_private_in_handoff () {
    import os, glob

    deploy = d.getVar("DEPLOY_DIR_IMAGE")
    handoff = os.path.join(deploy, "secureboot-handoff")
    if not os.path.isdir(handoff):
        return

    # Fail hard if private keys appear in handoff
    priv = glob.glob(os.path.join(handoff, "**", "*.key"), recursive=True)
    if priv:
        msg = "ERROR: Private key(s) found in secureboot-handoff:\n  " + "\n  ".join(priv)
        bb.fatal(msg)
}

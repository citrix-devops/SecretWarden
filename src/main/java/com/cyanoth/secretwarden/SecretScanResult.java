package com.cyanoth.secretwarden;

import com.cyanoth.secretwarden.structures.FoundSecretCollection;

import java.io.Serializable;

public class SecretScanResult implements Serializable {

    private FoundSecretCollection foundSecrets;
    private SecretScanStatus secretScanStatus;

    protected SecretScanResult() {
        secretScanStatus = SecretScanStatus.UNKNOWN;
    }

    public int countFoundSecrets() {
        return foundSecrets.count();
    }

    public FoundSecretCollection getFoundSecrets() {
        return foundSecrets;
    }

    public void setFoundSecrets(FoundSecretCollection foundSecrets) {
        this.foundSecrets = foundSecrets;
    }

    public SecretScanStatus getSecretScanStatus() {
        return secretScanStatus;
    }

    public void setSecretScanStatus(SecretScanStatus secretScanStatus) {
        this.secretScanStatus = secretScanStatus;
    }

}

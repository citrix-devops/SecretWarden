package com.cyanoth.secretwarden;

import com.cyanoth.secretwarden.collections.FoundSecretCollection;

import java.io.Serializable;

/**
 * Output from a secret scan, containing the found secrets & scan status.
 * Children of this class may add further metadata - such as where the scan took place.
 */
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

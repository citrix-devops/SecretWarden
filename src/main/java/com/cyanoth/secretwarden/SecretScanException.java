package com.cyanoth.secretwarden;

/**
 * Exception intended for use when a failure occurs during a secret scan.
 */
public class SecretScanException extends Exception {

    public SecretScanException (Exception e) {
        super("Exception occurred during a secret scan.", e);
    }
}

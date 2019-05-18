package com.cyanoth.secretwarden;

public class SecretScanException extends Exception {

    public SecretScanException (Exception e) {
        super("Exception occurred during a secret scan.", e);
    }
}

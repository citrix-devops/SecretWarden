package com.cyanoth.secretwarden;

public interface SecretScanner {

    // Interface which will allow for possible future changes such as scanning pull requests, branches, repositories.

    SecretScanResult scan(Boolean force) throws SecretScanException;

}

package com.cyanoth.secretwarden;

public interface SecretScanner {

    // Interface which will allows for possible future implementations such as scanning pull requests, branches, repositories.

    SecretScanResult scan(Boolean force) throws SecretScanException;

}

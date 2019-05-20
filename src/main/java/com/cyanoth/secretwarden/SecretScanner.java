package com.cyanoth.secretwarden;

/**
 * Interface which all secret scan varieties must implement (pull request scan, repository scan, branch scan etc.)
 */
public interface SecretScanner {

    SecretScanResult scan(Boolean force) throws SecretScanException;

}

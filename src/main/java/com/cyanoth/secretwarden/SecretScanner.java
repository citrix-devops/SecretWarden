package com.cyanoth.secretwarden;

import javax.annotation.Nullable;

/**
 * Interface which all secret scan varieties must implement (pull request scan, repository scan, branch scan etc.)
 */
public interface SecretScanner {

    @Nullable
    SecretScanResult scan(Boolean force) throws SecretScanException;

}

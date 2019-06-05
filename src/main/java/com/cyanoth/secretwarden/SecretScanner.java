package com.cyanoth.secretwarden;

import javax.annotation.Nullable;
import java.util.concurrent.locks.Lock;

/**
 * Interface which all secret scan varieties must implement (pull request scan, repository scan, branch scan etc.)
 */
public interface SecretScanner {

    /**
     * Method that should contain logic to perform a scan depending on scope (pull request/repository/branch etc)
     * @param force Intended for logic to bypass the cache if true
     * @return Result of the scan (such as found secrets)
     * @throws SecretScanException Exception that occurs during a scan to cause it to fail
     */
    @Nullable
    SecretScanResult scan(Boolean force) throws SecretScanException;

    @Nullable
    default Lock getScanLock(Object scope) {
        return null;
    }
}

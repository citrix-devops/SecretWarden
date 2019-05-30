package com.cyanoth.secretwarden.repository.branch;

import com.cyanoth.secretwarden.SecretScanException;
import com.cyanoth.secretwarden.SecretScanResult;
import com.cyanoth.secretwarden.SecretScanner;

/**
 * Secret Scanner for scanning repository branches.
 */
public class BranchSecretScanner implements SecretScanner {
    @Override
    public SecretScanResult scan(Boolean force) throws SecretScanException {
        return null;
    }
}

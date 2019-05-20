package com.cyanoth.secretwarden.repository;

import com.cyanoth.secretwarden.SecretScanException;
import com.cyanoth.secretwarden.SecretScanResult;
import com.cyanoth.secretwarden.SecretScanner;

/**
 * Secret Scanner for scanning Repositories
 */
public class RepositorySecretScanner implements SecretScanner {

    @Override
    public SecretScanResult scan(Boolean force) throws SecretScanException {
        return null;
    }
}

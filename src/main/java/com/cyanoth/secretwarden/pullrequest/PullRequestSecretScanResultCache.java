package com.cyanoth.secretwarden.pullrequest;

import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheFactory;
import com.atlassian.cache.CacheSettings;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cyanoth.secretwarden.SecretScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

// [1] https://docs.atlassian.com/atlassian-cache-api/2.2.0/atlassian-cache-api/apidocs/com/atlassian/cache/CacheFactory.html
// [2] https://bitbucket.org/atlassian/atlassian-spring-scanner/src/1.2.x/

@Component
public class PullRequestSecretScanResultCache extends SecretScanResult {
    private static final Logger log = LoggerFactory.getLogger(PullRequestSecretScanResultCache.class);
    private final CacheFactory cacheFactory;
    private final CacheSettings cacheSettings;

    private Cache<String, PullRequestSecretScanResult> _scanResultCache = null; // Use cache() to access

    @Autowired
    public PullRequestSecretScanResultCache(@ComponentImport final CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        this.cacheSettings = new CacheSettingsBuilder().remote().
                replicateViaCopy().
                expireAfterWrite(3, TimeUnit.DAYS).
                maxEntries(Integer.MAX_VALUE).build();
    }

    private Cache<String, PullRequestSecretScanResult> cache() {
        synchronized (this) {
            if (this._scanResultCache == null) {
                final String CACHE_NAME = "com.cyanoth.secretwarden:PullRequestSecretScanResultCache";
                this._scanResultCache = this.cacheFactory.getCache(CACHE_NAME, null, cacheSettings);
                log.debug("SecretWarden: PullRequestSecretScanResult scan initialised!");
            }

            return this._scanResultCache;
        }
    }

    @Nullable
    public PullRequestSecretScanResult get(PullRequest pullRequest) {
        return cache().get(genCacheKey(pullRequest));
    }

    public void put(PullRequest pullRequest, PullRequestSecretScanResult scanResult) {
        cache().put(genCacheKey(pullRequest), scanResult);
    }

    public void evict(PullRequest pullRequest) {
        cache().remove(genCacheKey(pullRequest));
    }

    public void cleanCache() {
        cache().removeAll();
    }

    public String genCacheKey(PullRequest pullRequest) {
        return String.valueOf(pullRequest.getToRef().getRepository().getId()) + "_PR" + pullRequest.getId();
    }
}

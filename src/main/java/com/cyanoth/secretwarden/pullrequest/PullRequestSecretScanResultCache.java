package com.cyanoth.secretwarden.pullrequest;

import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheFactory;
import com.atlassian.cache.CacheSettings;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

// [1] https://docs.atlassian.com/atlassian-cache-api/2.2.0/atlassian-cache-api/apidocs/com/atlassian/cache/CacheFactory.html
// [2] https://bitbucket.org/atlassian/atlassian-spring-scanner/src/1.2.x/

@Component
public class PullRequestSecretScanResultCache {
    private static final Logger log = LoggerFactory.getLogger(PullRequestSecretScanResultCache.class);
    private final CacheFactory cacheFactory;
    private final CacheSettings cacheSettings;

    private Cache<String, PullRequestSecretScanResult> _scanResultCache = null; // Use cache() for access

    @Autowired
    public PullRequestSecretScanResultCache(@ComponentImport final CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
        this.cacheSettings = new CacheSettingsBuilder().remote().
                replicateViaCopy().
                expireAfterAccess(3, TimeUnit.DAYS).
                maxEntries(Integer.MAX_VALUE).build();
    }

    @Nullable
    PullRequestSecretScanResult get(@Nonnull PullRequest pullRequest) {
        return get(pullRequest.getToRef().getRepository().getProject().getKey(),
                        pullRequest.getToRef().getRepository().getSlug(),
                        pullRequest.getId());
    }

    @Nullable
    public PullRequestSecretScanResult get(@Nonnull String projectKey, @Nonnull String repoSlug, long pullRequestId) {
        final String cacheKey = genCacheKey(projectKey, repoSlug, pullRequestId);
        return cache().containsKey(cacheKey) ? cache().get(cacheKey): null;
    }

    public void put(@Nonnull PullRequest pullRequest, @Nonnull PullRequestSecretScanResult scanResult) {
        put(pullRequest.getToRef().getRepository().getProject().getKey(),
                pullRequest.getToRef().getRepository().getSlug(),
                pullRequest.getId(), scanResult);
    }

    public void put(@Nonnull String projectKey, @Nonnull String repoSlug, long pullRequestId, @Nonnull PullRequestSecretScanResult scanResult) {
        cache().put(genCacheKey(projectKey, repoSlug, pullRequestId), scanResult);
    }

    public void clear() {
        cache().removeAll();
    }

    @NotNull
    private String genCacheKey(String projectKey, String repoSlug, Long pullRequestId) {
        return projectKey + "__" +  repoSlug + "__" + pullRequestId;
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
}

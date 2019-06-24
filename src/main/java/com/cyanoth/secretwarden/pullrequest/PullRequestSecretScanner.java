package com.cyanoth.secretwarden.pullrequest;

import com.atlassian.bitbucket.concurrent.LockService;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.cyanoth.secretwarden.collections.MatchRuleSet;
import com.cyanoth.secretwarden.SecretScanException;
import com.cyanoth.secretwarden.SecretScanStatus;
import com.cyanoth.secretwarden.SecretScanner;
import com.cyanoth.secretwarden.config.MatchRuleSetCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.concurrent.locks.Lock;

/**
 * Take a pull request from Bitbucket and handle the flow of a secret scan, including returning results.
 */
public class PullRequestSecretScanner implements SecretScanner {
    private static final Logger log = LoggerFactory.getLogger(PullRequestSecretScanner.class);
    private final PullRequestSecretScanResultCache pullRequestSecretScanCache;
    private final MatchRuleSetCache matchRuleSetCache;
    private final LockService lockService;
    private final PullRequestService pullRequestService;
    private final PullRequest pullRequest;

    PullRequestSecretScanner(PullRequestService service,
                             PullRequest pullRequest,
                             LockService lockService,
                             PullRequestSecretScanResultCache pullRequestSecretScanCache,
                             MatchRuleSetCache matchRuleSetCache) {
        this.pullRequestService = service;
        this.pullRequest = pullRequest;
        this.pullRequestSecretScanCache = pullRequestSecretScanCache;
        this.matchRuleSetCache = matchRuleSetCache;
        this.lockService = lockService;
    }

    /**
     * @param force Do not check cache. Scan even if the result is in the cache
     * @return Results of the secret scan (including but not limited to, found secrets & pull request last updated)
     * @throws SecretScanException An handled exception occurred during the scan but means results are incomplete
     */
    @Override
    public PullRequestSecretScanResult scan(Boolean force) throws SecretScanException {
        log.debug(String.format("SecretWarden scanning pull request: %s", prString()));
        PullRequestSecretScanResult scan = null;

        try {
            if (!force)
                scan = getCachedSecretScan(pullRequest);

            // Check if the entry in the cache still represents the latest version of the the pull-request. If so, use that, skip scan.
            if (scan != null) {
                if (scan.getPullRequestLastUpdated().compareTo(pullRequest.getUpdatedDate()) == 0) {
                    // In a multi-node setup, a scan might be taking place on another node. So we need to check its state
                    scan = checkOngoingScan(pullRequest);
                    return scan;
                } else {
                    // When the pull request is updated for any reason (commits added/removed, and not wanted but: comments added)
                    log.debug(String.format("SecretWarden has already scanned the pull request: %s but the information is out of date." +
                            " The pull request will be rescanned for secrets", prString()));
                }
            }

            Lock scanLock = getScanLock(pullRequest);

            // For performance & collision reasons, in a cluster - we must make sure that only one node scans the PullRequest
            // If no other node is scanning the pull-request, a lock is obtained and the pull request is scanned
            // There is a unlikely chance of a race condition here - so if the lock wasn't obtained, revert back to polling cache
            // We do _NOT_ use PullRequestLock because that operates on a PullRequest object (which isn't going to be modified)
            // [1] https://developer.atlassian.com/server/bitbucket/how-tos/cluster-safe-plugins/
            if (scanLock != null && scanLock.tryLock()) {
                try {
                    log.debug(String.format("SecretWarden has obtained a clusterlock to scan the pull-request %s", prString()));
                    scan = doSecretScan(pullRequest);

                } finally {
                    scanLock.unlock();
                }
            } else {
                // This block is very unlikely. Since an IN_PROGRESS scan will be found in the cache before this.
                // But there is a small possibility if the cache entry hasn't been made (race condition), so we must handle it
                log.info(String.format("SecretWarden failed to obtain a clusterlock to scan the pull-request %s (another node scanning?)",
                        prString()));
                scan = checkOngoingScan(pullRequest);
            }

            return scan;
        } catch (Exception e) {
            if (scan != null)
                scan.setSecretScanStatus(SecretScanStatus.FAILED);
            throw new SecretScanException(e);
        }
    }

    /**
     * Performs a secret scan on a pull request.
     *
     * @param pullRequest Pull request in question to scan
     * @return Secret Scan Results (includes FoundSecrets)
     */
    private PullRequestSecretScanResult doSecretScan(PullRequest pullRequest) {
        long scanStartTime = 0;
        PullRequestSecretScanResult scanResult;

        if (log.isDebugEnabled()) // Debug mode will time how long it takes
            scanStartTime = System.currentTimeMillis();

        log.debug(String.format("SecretWarden is performing a scan on pull-request %s", prString()));

        scanResult = new PullRequestSecretScanResult(pullRequest.getUpdatedDate());
        scanResult.setSecretScanStatus(SecretScanStatus.IN_PROGRESS);
        pullRequestSecretScanCache.put(pullRequest, scanResult);

        MatchRuleSet matchRuleSet = matchRuleSetCache.getRuleSet(); // Load once for entirety of the scan

        scanResult.setFoundSecrets(new ChangeStreamer(pullRequestService, matchRuleSet).scan(pullRequest).getFoundSecrets());
        scanResult.setSecretScanStatus(SecretScanStatus.COMPLETED);
        pullRequestSecretScanCache.put(pullRequest, scanResult);

        log.info(String.format("SecretWarden scanned the pull request: %s and has found %d secret(s).",
                prString(), scanResult.countFoundSecrets()));

        if (log.isDebugEnabled()) {
            long elapsedTime = System.currentTimeMillis() - scanStartTime;
            log.debug(String.format("SecretWarden took %d milliseconds to scan the pull request: %s", elapsedTime, prString()));
        }

        return scanResult;
    }

    /**
     * Retrieves a scan result from a cache, then check the scan status. Continue polling if scan is in progress
     * This is important in a multi-node setup, where another node may be scanning.
     * This is also important on large scans, where a user views a PR backs-out and the scan continues.
     * Need to periodically poll whether the scan has completed or not. Timeout logic is a fail-safe
     * @param pullRequest The pull-request a secret scan should or is taking place on
     * @return ScanResult
     */
    private PullRequestSecretScanResult checkOngoingScan(PullRequest pullRequest) throws SecretScanException {
        final int MAX_ATTEMPTS = 300; // = 60 second timeout
        final int DELAY = 200;
        int curAttempts = 0;

        PullRequestSecretScanResult scan = null;
        try {
            while (curAttempts <= MAX_ATTEMPTS) {
                scan = getCachedSecretScan(pullRequest);
                if (scan == null)
                    throw new Exception(String.format("SecretWarden couldn't find the cached entry for PR: %s", prString()));

                SecretScanStatus status = scan.getSecretScanStatus();

                if (status == SecretScanStatus.IN_PROGRESS) {
                    if (curAttempts % 10 == 0)
                        log.debug(String.format("SecretWarden is reportedly still scanning the pull request: %s", prString()));

                    Thread.sleep(DELAY); // The scan is still on-going, wait a little bit then try again
                    curAttempts++;
                } else if (status == SecretScanStatus.FAILED || status == SecretScanStatus.UNKNOWN) {
                    log.debug(String.format("SecretWarden had previously attempted to scan a pull request: %s but failed",
                            prString()));
                    break;
                } else {
                    log.debug(String.format("SecretWarden has already scanned the pull request: %s and found %d secret(s).",
                            prString(), scan.countFoundSecrets()));
                    break;
                }
            }

            if (curAttempts >= MAX_ATTEMPTS) {
                throw new Exception(String.format("SecretScan has timed-out waiting for scan results for pull request: %s", prString()));
            }

        } catch (Exception e) {
            log.error("SecretWarden failed to get the scan results from cache!");
            throw new SecretScanException(e);
        }

        return scan;
    }

    @Nullable
    private PullRequestSecretScanResult getCachedSecretScan(PullRequest pullRequest) {
        return pullRequestSecretScanCache.get(pullRequest);
    }

    /**
     * Obtain a lock object unique to a pull request
     * @param scope PullRequest - PullRequest details used for a unique lock key
     * @return Lock object unique to a pull-request (can be null)
     */
    @Override
    public Lock getScanLock(Object scope) {
         if (!(scope instanceof PullRequest))
            return null;


        String lockKey = String.format("com.cyanoth.secretwarden.prscan.lock_%d_%d",
                pullRequest.getToRef().getRepository().getId(), pullRequest.getId());

        return lockService.getLock(lockKey);
    }

    private String prString() {
        return String.format("%s (PR: %d)", this.pullRequest.getToRef(), this.pullRequest.getId());
    }
}
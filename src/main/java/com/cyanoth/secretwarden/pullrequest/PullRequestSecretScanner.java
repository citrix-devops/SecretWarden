package com.cyanoth.secretwarden.pullrequest;

import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.cyanoth.secretwarden.collections.MatchRuleSet;
import com.cyanoth.secretwarden.SecretScanException;
import com.cyanoth.secretwarden.SecretScanStatus;
import com.cyanoth.secretwarden.SecretScanner;
import com.cyanoth.secretwarden.config.MatchRuleSetCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Take a pull request from Bitbucket and handle the flow of a secret scan, including returning results.
 */
public class PullRequestSecretScanner implements SecretScanner {
    private static final Logger log = LoggerFactory.getLogger(PullRequestSecretScanner.class);
    private final PullRequestSecretScanResultCache pullRequestSecretScanCache;
    private final MatchRuleSetCache matchRuleSetCache;
    private final PullRequestService pullRequestService;
    private final PullRequest pullRequest;

    PullRequestSecretScanner(PullRequestService service,
                             PullRequest pullRequest,
                             PullRequestSecretScanResultCache pullRequestSecretScanCache,
                             MatchRuleSetCache matchRuleSetCache) {
        this.pullRequestService = service;
        this.pullRequest = pullRequest;
        this.pullRequestSecretScanCache = pullRequestSecretScanCache;
        this.matchRuleSetCache = matchRuleSetCache;
    }

    /**
     * @param force Do not check cache. Scan even if the result is in the cache
     * @return Results of the secret scan (including but not limited to, found secrets & pull request last updated)
     * @throws SecretScanException An handled exception occurred during the scan but means results are incomplete
     */
    @Override
    public PullRequestSecretScanResult scan(Boolean force) throws SecretScanException {
        log.debug(String.format("SecretWarden scanning pull request: %s (PR ID: %d)", pullRequest.getToRef(), pullRequest.getId()));
        PullRequestSecretScanResult scan = null;

        try {
            if (!force)
                scan = pullRequestSecretScanCache.get(pullRequest);

            // Check if the entry in the cache still represents the latest version of the the pull-request. If so, use that, skip scan.
            if (scan != null) {
                if (scan.getPullRequestLastUpdated().compareTo(pullRequest.getUpdatedDate()) == 0) {
                    log.debug(String.format("SecretWarden has already scanned the pull request: %s (PR: %d) and found %d secret(s).",
                        pullRequest.getToRef(), pullRequest.getId(), scan.countFoundSecrets()));
                    return scan;
                } else { // When the pull request is updated for any reason (commits added/removed, and not wanted but: comments added)
                    log.debug(String.format("SecretWarden has already scanned the pull request: %s (ID: %d) but the information is out of date." +
                            " The pull request will be rescanned for secrets", pullRequest.getToRef(), pullRequest.getId()));
                }
            }

            long scanStartTime = 0;
            if (log.isDebugEnabled()) // Debug mode will time how long it takes
                scanStartTime = System.currentTimeMillis();

            // IMPROVE_ME: It may be worth considering putting a cluster lock here so that only one node will run a secret scan at a time.
            // At present, if two (or more) user's view a pull-request that hasn't been scanned or it is still in-progress and the user's are on
            // different nodes, then a scan will be executed on each node. With the current replicated cache, if the scenario where more than one node
            // performs a scan at the same time and ultimately, reports results at different times. Its just this is a unnecessary waste of resources.
            scan = new PullRequestSecretScanResult(pullRequest.getUpdatedDate());
            scan.setSecretScanStatus(SecretScanStatus.IN_PROGRESS);
            pullRequestSecretScanCache.put(pullRequest, scan);

            MatchRuleSet matchRuleSet = matchRuleSetCache.getRuleSet(); // Load once for entirety of the scan

            scan.setFoundSecrets(new ChangeStreamer(pullRequestService, matchRuleSet).scan(pullRequest).getFoundSecrets());
            scan.setSecretScanStatus(SecretScanStatus.COMPLETED);
            pullRequestSecretScanCache.put(pullRequest, scan);

            log.info(String.format("SecretWarden scanned the pull request: %s (ID: %d) and has found %d secret(s).",
                                    pullRequest.getToRef(), pullRequest.getId(), scan.countFoundSecrets()));

            if (log.isDebugEnabled()) {
                long elapsedTime = System.currentTimeMillis() - scanStartTime;
                log.debug(String.format("SecretWarden took %d milliseconds to scan the pull request: %s (ID %d)",
                        elapsedTime, pullRequest.getToRef(), pullRequest.getId()));
            }

            return scan;
        }
        catch (Exception e) {
            if (scan != null)
                scan.setSecretScanStatus(SecretScanStatus.FAILED);
            throw new SecretScanException(e);
        }
    }
}
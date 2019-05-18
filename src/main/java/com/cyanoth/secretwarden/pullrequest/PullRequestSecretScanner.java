package com.cyanoth.secretwarden.pullrequest;

import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.cyanoth.secretwarden.SecretScanException;
import com.cyanoth.secretwarden.SecretScanStatus;
import com.cyanoth.secretwarden.SecretScanner;
import com.cyanoth.secretwarden.config.MatchRuleSet;
import com.cyanoth.secretwarden.pullrequest.internal.ChangeStreamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullRequestSecretScanner implements SecretScanner {
    private static final Logger log = LoggerFactory.getLogger(PullRequestSecretScanner.class);
    private final PullRequestSecretScanResultCache pullRequestSecretScanCache;
    private final PullRequestService pullRequestService;
    private final PullRequest pullRequest;


    PullRequestSecretScanner(PullRequestService service,
                             PullRequest pullRequest,
                             PullRequestSecretScanResultCache pullRequestSecretScanCache) {
        this.pullRequestService = service;
        this.pullRequest = pullRequest;
        this.pullRequestSecretScanCache = pullRequestSecretScanCache;
    }


    @Override
    public PullRequestSecretScanResult scan(Boolean force) throws SecretScanException {
        log.debug(String.format("SecretWarden scanning pull request: %s (PR ID: %d)", pullRequest.getFromRef(), pullRequest.getId()));
        PullRequestSecretScanResult scan = null;

        try {
            MatchRuleSet ruleSet = new MatchRuleSet();
            ruleSet.reloadRuleSet(); // TODO: REMOVE ME! TESTING ONLY. This needs to become a component

            if (!force)
                scan = pullRequestSecretScanCache.get(pullRequest);

            // Check if the entry in the cache still represents the latest version of the the pull-request. If so, use that, skip scan.
            if (scan != null) {
                if (scan.getPullRequestLastUpdated().compareTo(pullRequest.getUpdatedDate()) == 0) {
                    log.debug(String.format("SecretWarden has already scanned the pull request: %s (ID: %d) and found %d secret(s).",
                        pullRequest.getFromRef(), pullRequest.getId(), scan.countFoundSecrets()));
                    return scan;
                } else {
                    log.debug(String.format("SecretWarden has already scanned the pull request: %s (ID: %d) but the information is out of date." +
                            "Will rescan", pullRequest.getFromRef(), pullRequest.getId()));
                }
            }

            long scanStartTime = 0;
            if (log.isDebugEnabled())
                scanStartTime = System.currentTimeMillis();

            // IMPROVE_ME: It may be worth considering putting a cluster lock here so that only one node will run a secret scan at a time.
            // At present, if two (or more) user's view a pull-request that hasn't been scanned or it is still in-progress and the user's are on
            // different nodes, then a scan will be executed on each node. Due to replicated cache, it shouldn't be a problem if more than one node
            // does the scan at the same time and ultimately, reports results at different times. Its just this is a unnecessary waste of resources.
            scan = new PullRequestSecretScanResult(pullRequest.getId(), pullRequest.getFromRef().toString(), pullRequest.getUpdatedDate());
            scan.setSecretScanStatus(SecretScanStatus.IN_PROGRESS);
            scan.setFoundSecrets(new ChangeStreamer(pullRequestService, ruleSet).scan(pullRequest).getFoundSecrets());
            scan.setSecretScanStatus(SecretScanStatus.COMPLETED);
            pullRequestSecretScanCache.put(pullRequest, scan);
            log.info(String.format("SecretWarden scanned the pull request: %s (ID: %d) and has found %d secret(s).",
                                    pullRequest.getFromRef(), pullRequest.getId(), scan.countFoundSecrets()));

            if (log.isDebugEnabled()) {
                long elaspedTime = System.currentTimeMillis() - scanStartTime;
                log.debug(String.format("SecretWarden took %d milliseconds to scan the pull request: %s (ID %d)",
                        elaspedTime, pullRequest.getFromRef(), pullRequest.getId()));
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
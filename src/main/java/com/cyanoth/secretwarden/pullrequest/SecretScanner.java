package com.cyanoth.secretwarden.pullrequest;

import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.pull.PullRequestService;
import com.cyanoth.secretwarden.config.MatchSecretRuleSet;
import com.cyanoth.secretwarden.structures.FoundSecretCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SecretScanner {
    private static final Logger log = LoggerFactory.getLogger(SecretScanner.class);


    private final PullRequestService pullRequestService;


    public SecretScanner(PullRequestService pullRequestService) {
        this.pullRequestService = pullRequestService;
    }


    public FoundSecretCollection scan(PullRequest pullRequest, Boolean force) {

        log.info(String.format("SecretWarden scanning a pull request: %s (PR ID: %d)", pullRequest.getFromRef(), pullRequest.getId()));
        MatchSecretRuleSet ruleSet = new MatchSecretRuleSet();
        ruleSet.reloadRuleSet(); // TODO: REMOVE ME! TESTING ONLY

        FoundSecretCollection secretsInPullRequest = new ChangeStreamer(pullRequestService, ruleSet).scan(pullRequest).getFoundSecrets();

        log.debug(String.format("SecretWarden scan has completed on the pull request: %s and has found %d secret(s)",
                pullRequest.getFromRef(), secretsInPullRequest.count()));

        return secretsInPullRequest;


    }


}

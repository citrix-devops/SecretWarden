package com.cyanoth.secretwarden.pullrequest;

import com.atlassian.bitbucket.content.AbstractChangeCallback;
import com.atlassian.bitbucket.content.Change;
import com.atlassian.bitbucket.content.ChangeType;
import com.atlassian.bitbucket.pull.*;
import com.cyanoth.secretwarden.config.MatchSecretRuleSet;
import com.cyanoth.secretwarden.structures.FoundSecretCollection;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;

/**
 * INTERNAL: Streams the differences of pull-requests to difference
 */
class ChangeStreamer {
    private final PullRequestService pullRequestService;
    private final MatchSecretRuleSet ruleSet;
    private final FoundSecretCollection totalFoundSecrets;

    ChangeStreamer(PullRequestService pullRequestService, MatchSecretRuleSet ruleSet) {
        this.pullRequestService = pullRequestService;
        this.ruleSet = ruleSet;
        totalFoundSecrets = new FoundSecretCollection();
    }

    /**
     * Scan a pull-request for any changed source files. The changed source files then go onto DiffMatcher
     * where the changed code inside the source file will be scanned for secrets. DiffMatcher returns
     * any FoundSecret, all of which get collected into a single set and returned
     * @param pullRequest The pull request to stream changes and search for secrets
     * @return This object for chaining. Returns once the scan is complete.
     */
    ChangeStreamer scan(PullRequest pullRequest) {
        scanPullRequestChangesForSecrets(pullRequest);
        return this;
    }

    @NotNull
    FoundSecretCollection getFoundSecrets() {
        return totalFoundSecrets;
    }

    @NotNull
    private void scanPullRequestChangesForSecrets(PullRequest pullRequest) {
        PullRequestChangesRequest changeRequest = new PullRequestChangesRequest.Builder(pullRequest).
                changeScope(PullRequestChangeScope.ALL)  // With Scope ALL, it becomes unnecessary to specify the since/until commit range
                .withComments(false)
                .build();

        pullRequestService.streamChanges(changeRequest, new AbstractChangeCallback() {
            @Override
            public boolean onChange(@Nonnull Change change) {
                if (change.getType() == ChangeType.ADD || change.getType() == ChangeType.MODIFY) {
                    totalFoundSecrets.merge(scanChangedFileDifferencesForSecrets(pullRequest, change.getPath().toString()));
                }
                return true; // _Really_ do not care about this value, but its super() requires returning something
            }
        });
    }

    @NotNull
    private FoundSecretCollection scanChangedFileDifferencesForSecrets(PullRequest pullRequest, String targetFilePath) {
        final PullRequestDiffRequest fileDifference = new PullRequestDiffRequest.Builder(pullRequest, targetFilePath)
                .withComments(false)
                .build();

        final DiffMatcher matchSecretCallback = new DiffMatcher(ruleSet);
        pullRequestService.streamDiff(fileDifference, matchSecretCallback);
        return matchSecretCallback.getFoundSecrets();
    }

}
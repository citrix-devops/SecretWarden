package com.cyanoth.secretwarden;

import com.atlassian.bitbucket.content.AbstractChangeCallback;
import com.atlassian.bitbucket.content.Change;
import com.atlassian.bitbucket.content.ChangeType;
import com.atlassian.bitbucket.pull.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

public class PullRequestSecretScanner {
    private static final Logger log = LoggerFactory.getLogger(PullRequestSecretScanner.class);
    private final PullRequestService pullRequestService;
    private final Collection<String> foundSecrets = new HashSet<>();
    private boolean scanCompleted = false;

    PullRequestSecretScanner(PullRequestService pullRequestService) {
        this.pullRequestService = pullRequestService;
    }

    PullRequestSecretScanner scan(PullRequest pullRequest) {
        scanPullRequestChangesForSecrets(pullRequest);
        scanCompleted = true;
        return this;
    }

    public int countFoundSecrets() {
        // if len(foundSecrets) > 0

        if (!scanCompleted) {
            // log serve: tried to find scan result when a scan has not completed!
        }

        return 10;
    }



    public Collection<String> getSecrets() {
        // IMPLEMENT ME
        if (!scanCompleted) {
            // log serve: tried to find scan result when a scan has not completed!

        }

        return foundSecrets;
    }

    @NotNull
    private void scanPullRequestChangesForSecrets(PullRequest pullRequest) {
         // Need to be careful here, duplicate secrets will be counted as 1. Need to include file name

        PullRequestChangesRequest changeRequest = new PullRequestChangesRequest.Builder(pullRequest).
                changeScope(PullRequestChangeScope.ALL)  // With Scope ALL, it becomes unnecessary to specify (since/until) the commit range
                .withComments(false) // This is default true. Checking secrets in comments is out-of-scope! so don't waste resources retrieving them.
                .build();

        pullRequestService.streamChanges(changeRequest, new AbstractChangeCallback() {
            @Override
            public boolean onChange(@Nonnull Change change) throws IOException {
                if (change.getType() == ChangeType.ADD || change.getType() == ChangeType.MODIFY) {
                    foundSecrets.addAll(scanChangedFileDifferencesForSecrets(pullRequest, change.getPath().toString()));
                }
                return true; // TODO: Check whether we actually care about this
            }
        });
    }

    @NotNull
    private Collection<String> scanChangedFileDifferencesForSecrets(PullRequest pullRequest, String targetFilePath) {
        final PullRequestDiffRequest fileDifference = new PullRequestDiffRequest.Builder(pullRequest, targetFilePath)
                .withComments(false) // This is default true. Checking secrets in comments is out-of-scope! so don't waste resources retrieving them.
                .build();

        final MatchSecretCollector matchSecretCallback = new MatchSecretCollector();
        pullRequestService.streamDiff(fileDifference, matchSecretCallback);
        return matchSecretCallback.getFoundSecrets();
    }

}




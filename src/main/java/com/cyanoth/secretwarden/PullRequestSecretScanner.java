package com.cyanoth.secretwarden;

import com.atlassian.bitbucket.content.AbstractChangeCallback;
import com.atlassian.bitbucket.content.Change;
import com.atlassian.bitbucket.content.ChangeType;
import com.atlassian.bitbucket.pull.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;


public class PullRequestSecretScanner {
    private static final Logger log = LoggerFactory.getLogger(PullRequestSecretScanner.class);
    private final PullRequestService pullRequestService;
    private final Collection<FoundSecret> foundSecrets = new HashSet<>();
    private boolean scanCompleted = false;

    PullRequestSecretScanner(PullRequestService pullRequestService) {
        this.pullRequestService = pullRequestService;
    }

    public PullRequestSecretScanner scan(PullRequest pullRequest) {
        scanPullRequestChangesForSecrets(pullRequest);
        scanCompleted = true;
        return this;
    }

    public int countFoundSecrets() throws ScanIncompleteException {
        if (!scanCompleted)
            throw new ScanIncompleteException();

        return foundSecrets.size();
    }


    public Collection<FoundSecret> getSecrets() throws ScanIncompleteException {
        if (!scanCompleted)
            throw new ScanIncompleteException();

        return foundSecrets;
    }

    @NotNull
    private void scanPullRequestChangesForSecrets(PullRequest pullRequest) {
        PullRequestChangesRequest changeRequest = new PullRequestChangesRequest.Builder(pullRequest).
                changeScope(PullRequestChangeScope.ALL)  // With Scope ALL, it becomes unnecessary to specify since/until commit range
                .withComments(false)
                .build();

        // As strongly recommended by the documentation [1] intentionally extend the ABSTRACT version to prevent breakage from future API changes.
        // [1] https://docs.atlassian.com/bitbucket-server/javadoc/5.16.4/api/reference/com/atlassian/bitbucket/content/ChangeCallback.html
        pullRequestService.streamChanges(changeRequest, new AbstractChangeCallback() {
            @Override
            public boolean onChange(@Nonnull Change change) {
                if (change.getType() == ChangeType.ADD || change.getType() == ChangeType.MODIFY) {
                    foundSecrets.addAll(scanChangedFileDifferencesForSecrets(pullRequest, change.getPath().toString()));
                }
                return true; // _Really_ do not care about this value, but it is from an abstract method so must return something
            }
        });
    }

    @NotNull
    private Collection<FoundSecret> scanChangedFileDifferencesForSecrets(PullRequest pullRequest, String targetFilePath) {
        final PullRequestDiffRequest fileDifference = new PullRequestDiffRequest.Builder(pullRequest, targetFilePath)
                .withComments(false)
                .build();

        final FoundSecretCollector matchSecretCallback = new FoundSecretCollector();
        pullRequestService.streamDiff(fileDifference, matchSecretCallback);
        return matchSecretCallback.getFoundSecrets();
    }

}
package com.cyanoth.secretwarden.pullrequest;

import com.cyanoth.secretwarden.SecretScanResult;

import java.io.Serializable;
import java.util.Date;

class PullRequestSecretScanResult extends SecretScanResult implements Serializable {

    private final long pullRequestId;
    private final String pullRequestFromRef;
    private final Date pullRequestLastUpdated;

    // repo name?
    // project name?

    PullRequestSecretScanResult(long prId, String prFromRef, Date prLastUpdated) {
        this.pullRequestId = prId;
        this.pullRequestFromRef = prFromRef;
        this.pullRequestLastUpdated = prLastUpdated;
    }

    public Date getPullRequestLastUpdated() {
        return pullRequestLastUpdated;
    }

    public String getPullRequestFromRef() {
        return pullRequestFromRef;
    }

    public long getPullRequestId() {
        return pullRequestId;
    }
}

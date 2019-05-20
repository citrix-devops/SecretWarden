package com.cyanoth.secretwarden.pullrequest;

import com.cyanoth.secretwarden.SecretScanResult;

import java.io.Serializable;
import java.util.Date;


public class PullRequestSecretScanResult extends SecretScanResult implements Serializable {

    private final Date pullRequestLastUpdated;

    public PullRequestSecretScanResult(Date prLastUpdated) {
        this.pullRequestLastUpdated = prLastUpdated;
    }

    public Date getPullRequestLastUpdated() {
        return pullRequestLastUpdated;
    }

}

package com.cyanoth.secretwarden.pullrequest;

import com.cyanoth.secretwarden.SecretScanResult;

import java.io.Serializable;
import java.util.Date;


public class PullRequestSecretScanResult extends SecretScanResult implements Serializable {

    // Keep a record of when the pull request was lasted updated (scanned) so incase of any changes
    // The scan can be voided and re-ran.
    private final Date pullRequestLastUpdated;

    public PullRequestSecretScanResult(Date prLastUpdated) {
        this.pullRequestLastUpdated = prLastUpdated;
    }

    public Date getPullRequestLastUpdated() {
        return pullRequestLastUpdated;
    }

}

package com.cyanoth.secretwarden.pullrequest.rest;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.cyanoth.secretwarden.SecretScanStatus;
import com.cyanoth.secretwarden.pullrequest.PullRequestSecretScanResult;
import com.cyanoth.secretwarden.pullrequest.PullRequestSecretScanResultCache;
import com.cyanoth.secretwarden.FoundSecretCollection;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/prscan/result")
@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Scanned
public class ScanResult{
    private static final Logger log = LoggerFactory.getLogger(ScanResult.class);

    @GET
    @Path("/{projectKey}/{repoSlug}/{pullRequestId}")
    public Response getPullRequestSecretScanResult(@PathParam("projectKey") String projectKey, @PathParam("repoSlug") String repoSlug,
                                                   @PathParam("pullRequestId") Long pullRequestId,
                                                   PullRequestSecretScanResultCache pullRequestSecretScanResultCache)
    {
        PullRequestSecretScanResult result = pullRequestSecretScanResultCache.get(projectKey, repoSlug, pullRequestId);

        log.debug("REST. Result: Status:" + result.getSecretScanStatus().toString());
        log.debug("REST. Result: Secrets: " + result.getFoundSecrets().toString());
        
        if (result == null) {
            SecretScanStatus scanStatus = SecretScanStatus.UNKNOWN;
            String resultAsJson = new Gson().toJson(scanStatus);
            return Response.ok(resultAsJson).build();
        }
        else {
            SecretScanStatus scanStatus = result.getSecretScanStatus();
            FoundSecretCollection foundSecrets = result.getFoundSecrets();

            String resultAsJson = new Gson().toJson(foundSecrets);
            return Response.ok(resultAsJson).build();
        }
    }


}

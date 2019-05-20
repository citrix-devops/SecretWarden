package com.cyanoth.secretwarden.pullrequest.rest;

import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.cyanoth.secretwarden.pullrequest.PullRequestSecretScanResult;
import com.cyanoth.secretwarden.pullrequest.PullRequestSecretScanResultCache;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/prscan/result")
@Scanned
public class ScanResult{
    private static final Logger log = LoggerFactory.getLogger(ScanResult.class);
    private final PullRequestSecretScanResultCache pullRequestSecretScanResultCache;
    private final PermissionService permissionService;

    ScanResult(PullRequestSecretScanResultCache pullRequestSecretScanResultCache, PermissionService permissionService) {
        this.pullRequestSecretScanResultCache = pullRequestSecretScanResultCache;
        this.permissionService = permissionService;
    }

    /**
     * Retrieve the secret scan result from the pull request cache. If the secret scan result does not exist in cache
     * then 404 will be returned. This API does _NOT_ trigger a secret scan.
     * The intention for this is to serve data to the UI on the pull-request overview - hence why this is not a public API.
     * @param projectKey Bitbucket Project Key where the pull-request applies too.
     * @param repoSlug Bitbucket Repository Slug where the pull-request applies too.
     * @param pullRequestId Numeric ID of the pull-request
     * @return JSON of a single PullRequestSecretScan if it has recently been scanned. 404 otherwise.
     */
    @GET
    @Path("/{projectKey}/{repoSlug}/{pullRequestId}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getPullRequestSecretScanResult(@PathParam("projectKey") String projectKey, @PathParam("repoSlug") String repoSlug,
                                                   @PathParam("pullRequestId") Long pullRequestId)
    {
        PullRequestSecretScanResult result = pullRequestSecretScanResultCache.get(projectKey, repoSlug, pullRequestId);

        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        else {
            String resultAsJson = new Gson().toJson(result);
            return Response.ok(resultAsJson).build();
        }
    }

    @PUT
    @Path("/clearcache")
    public Response clearCache()
    {

        // TODO: This should require global administrator permission.

        pullRequestSecretScanResultCache.clear();
        return Response.ok("Cache Cleared!").build();
    }

}

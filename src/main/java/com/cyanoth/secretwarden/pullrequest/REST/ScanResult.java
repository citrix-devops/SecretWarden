package com.cyanoth.secretwarden.pullrequest.REST;

import com.atlassian.bitbucket.AuthorisationException;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.repository.RepositoryService;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cyanoth.secretwarden.pullrequest.PullRequestSecretScanResult;
import com.cyanoth.secretwarden.pullrequest.PullRequestSecretScanResultCache;
import com.google.gson.Gson;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Exposed REST endpoints to retrieve pull request secret scan result from cache
 *
 * [1] https://developer.atlassian.com/server/framework/atlassian-sdk/rest-plugin-module/
 */
@Path("/prscan/result")
@Scanned
public class ScanResult{
    private final PermissionService permissionService;
    private final RepositoryService repositoryService;
    private final PullRequestSecretScanResultCache pullRequestSecretScanResultCache;

    ScanResult(PullRequestSecretScanResultCache pullRequestSecretScanResultCache,
               @ComponentImport PermissionService permissionService,
               @ComponentImport RepositoryService repositoryService) {
        this.pullRequestSecretScanResultCache = pullRequestSecretScanResultCache;
        this.permissionService = permissionService;
        this.repositoryService = repositoryService;
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
                                                   @PathParam("pullRequestId") Long pullRequestId) {
        try {
            Repository repo = repositoryService.getBySlug(projectKey, repoSlug);

            if (repo == null)
                return Response.status(Response.Status.NOT_FOUND).build();

            if (!permissionService.hasRepositoryPermission(repo, Permission.REPO_READ)) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            PullRequestSecretScanResult result = pullRequestSecretScanResultCache.get(projectKey, repoSlug, pullRequestId);

            if (result == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            } else {
                return Response.ok(new Gson().toJson(result)).build();
            }
        } catch (AuthorisationException e) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }
}

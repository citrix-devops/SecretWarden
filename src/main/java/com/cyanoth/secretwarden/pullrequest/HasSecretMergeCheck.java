package com.cyanoth.secretwarden.pullrequest;

import com.atlassian.bitbucket.hook.repository.*;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.pull.*;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import com.cyanoth.secretwarden.SecretScanException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.Nonnull;

@Component("HasSecretMergeCheck")
@Scanned
public class HasSecretMergeCheck implements RepositoryMergeCheck {
    private static final Logger log = LoggerFactory.getLogger(HasSecretMergeCheck.class);

    private final PullRequestSecretScanResultCache pullRequestSecretScanCache;

    private final PermissionService permissionService;
    private final PullRequestService pullRequestService;


    @Autowired
    public HasSecretMergeCheck(@ComponentImport PermissionService permissionService,
                               @ComponentImport PullRequestService pullRequestService,
                               final PullRequestSecretScanResultCache pullRequestSecretScanResultCache) {
        this.permissionService = permissionService;
        this.pullRequestService = pullRequestService;
        this.pullRequestSecretScanCache = pullRequestSecretScanResultCache;  //OSGi Service
    }

    @Nonnull
    @Override
    public RepositoryHookResult preUpdate(@Nonnull PreRepositoryHookContext context,
                                          @Nonnull PullRequestMergeHookRequest request) {

        try {
            final PullRequest pullRequest = request.getPullRequest();
            final Repository repository = pullRequest.getToRef().getRepository();

            PullRequestSecretScanResult pullRequestScan = new PullRequestSecretScanner(pullRequestService,
                    pullRequest,pullRequestSecretScanCache).scan(false);

            int secretCount = pullRequestScan.countFoundSecrets();
            if (secretCount > 0) {
                if (!permissionService.hasRepositoryPermission(repository, Permission.REPO_ADMIN)) {
                    String s =  (secretCount > 1) ? "secrets" : "secret";
                    return RepositoryHookResult.rejected(String.format("%d %s identified in this pull-request.", secretCount, s),
                            String.format("This PR contains %d %s, therefore only a repository admin may merge this pull request.", secretCount, s));
                }
            }
        }
        // If an exception occurs performing this merge check, don't block the pull request from being merged - just log the exception that occurred.
        catch (SecretScanException e) {
           log.error("ERROR: HasSecretMergeCheck has failed before the secret scan has failed. Exception: ", e);
        }
        // Fail-safe, catch anything here intentionally so it bubble-up any further
        catch (Exception e) {
            log.error("ERROR: HasSecretMergeCheck has check for secrets. An unexpected exception occurred: ", e);
        }

        return RepositoryHookResult.accepted();
    }
}



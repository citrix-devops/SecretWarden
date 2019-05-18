package com.cyanoth.secretwarden.pullrequest.mergechecks;

import com.atlassian.bitbucket.hook.repository.*;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.pull.*;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cyanoth.secretwarden.pullrequest.SecretScanner;
import com.cyanoth.secretwarden.structures.FoundSecretCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.Nonnull;


@Component("HasSecretMergeCheck")
public class HasSecretMergeCheck implements RepositoryMergeCheck {
    private static final Logger log = LoggerFactory.getLogger(HasSecretMergeCheck.class);
    private final PermissionService permissionService;
    private final PullRequestService pullRequestService;
    @Autowired
    public HasSecretMergeCheck(@ComponentImport PermissionService permissionService,
                               @ComponentImport PullRequestService pullRequestService) {
        this.permissionService = permissionService;
        this.pullRequestService = pullRequestService;
    }

    @Nonnull
    @Override
    public RepositoryHookResult preUpdate(@Nonnull PreRepositoryHookContext context,
                                          @Nonnull PullRequestMergeHookRequest request) {

        try {
            final PullRequest pullRequest = request.getPullRequest();
            final Repository repository = pullRequest.getToRef().getRepository();

            FoundSecretCollection pullRequestScan = new SecretScanner(pullRequestService).scan(pullRequest,false);

            int count = pullRequestScan.count();
            if (count > 0) {
                if (!permissionService.hasRepositoryPermission(repository, Permission.REPO_ADMIN)) {
                    String s =  (count > 1) ? "secrets" : "secret";
                    return RepositoryHookResult.rejected(String.format("%d %s identified in this pull-request.", count, s),
                            String.format("This PR contains %d %s, therefore only a repository admin may merge this pull request.", count, s));
                }
            }
        }
        // If an exception occurs performing this merge check, don't block the pull request from being merged - just log that an error occurred.
        catch (Exception e) {
            log.error("ERROR: Failed to check run HasSecretMergeCheck. Unknown Exception Occurred:" + e.toString()); //TODO: TRACEBACK
        }

        return RepositoryHookResult.accepted();
    }
}



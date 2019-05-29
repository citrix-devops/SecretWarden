package com.cyanoth.secretwarden.pullrequest;

import com.atlassian.bitbucket.hook.repository.*;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.pull.*;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import com.cyanoth.secretwarden.SecretScanException;
import com.cyanoth.secretwarden.config.MatchRuleSetCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.Nonnull;

/**
 * Bitbucket Merge Check class. If enabled on project/repository, on a new pull request this gets invoked.
 * The merge check will invoke a secret scan, check how many secrets were found.
 * If one or more secrets were found, the merge will be blocked unless the user is a project/repository administrator.
 */
@Component("HasSecretMergeCheck")
@Scanned
public class HasSecretMergeCheck implements RepositoryMergeCheck {
    private static final Logger log = LoggerFactory.getLogger(HasSecretMergeCheck.class);
    private final PermissionService permissionService;
    private final PullRequestService pullRequestService;
    private final PullRequestSecretScanResultCache pullRequestSecretScanResultCache;
    private final MatchRuleSetCache matchRuleSetCache;

    @Autowired
    public HasSecretMergeCheck(@ComponentImport PermissionService permissionService,
                               @ComponentImport PullRequestService pullRequestService,
                               PullRequestSecretScanResultCache pullRequestSecretScanResultCache,
                               MatchRuleSetCache matchRuleSetCache) {
        this.permissionService = permissionService;
        this.pullRequestService = pullRequestService;
        this.pullRequestSecretScanResultCache = pullRequestSecretScanResultCache;
        this.matchRuleSetCache = matchRuleSetCache;
    }

    @Nonnull
    @Override
    public RepositoryHookResult preUpdate(@Nonnull PreRepositoryHookContext context,
                                          @Nonnull PullRequestMergeHookRequest request) {

        try {
            final PullRequest pullRequest = request.getPullRequest();
            final Repository repository = pullRequest.getToRef().getRepository();

            PullRequestSecretScanResult pullRequestScan = new PullRequestSecretScanner(pullRequestService, pullRequest,
                    pullRequestSecretScanResultCache, matchRuleSetCache).scan(false);

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
           log.error("ERROR: HasSecretMergeCheck has failed whilst performing a secret scan. Exception: ", e);
        }
        // Fail-safe, catch anything here intentionally so it doesn't bubble-up any further
        catch (Exception e) {
            log.error("ERROR: HasSecretMergeCheck has failed to check for secrets due to an unexpected exception: ", e);
        }

        return RepositoryHookResult.accepted();
    }
}



package com.cyanoth.secretwarden;

import com.atlassian.bitbucket.hook.repository.*;
import com.atlassian.bitbucket.i18n.I18nService;
import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.permission.PermissionService;
import com.atlassian.bitbucket.pull.*;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.Nonnull;


@Component("HasSecretMergeCheck")
public class HasSecretMergeCheck implements RepositoryMergeCheck {
    private static final Logger log = LoggerFactory.getLogger(HasSecretMergeCheck.class);
    private final PermissionService permissionService;
    private final I18nService i18nService;
    private final PullRequestService pullRequestService;
    @Autowired
    public HasSecretMergeCheck(@ComponentImport I18nService i18nService,
                               @ComponentImport PermissionService permissionService,
                               @ComponentImport PullRequestService pullRequestService) {
        this.i18nService = i18nService;
        this.permissionService = permissionService;
        this.pullRequestService = pullRequestService;
    }

    @Nonnull
    @Override
    public RepositoryHookResult preUpdate(@Nonnull PreRepositoryHookContext context,
                                          @Nonnull PullRequestMergeHookRequest request) {

        try {
            FindSecretRuleSet.reloadRuleSet(); // TODO: REMOVE ME !!1 TESTING ONLY: This should be loaded once on plugin load / on config changes

            final PullRequest pullRequest = request.getPullRequest();
            final Repository repository = pullRequest.getToRef().getRepository();

            PullRequestSecretScanner scannedPullRequest = new PullRequestSecretScanner(pullRequestService).scan(pullRequest);

            int count = scannedPullRequest.countFoundSecrets();
            if (count > 0) {
                if (!permissionService.hasRepositoryPermission(repository, Permission.REPO_ADMIN)) {
                    String s =  (count > 1) ? "secrets" : "secret";
                    return RepositoryHookResult.rejected(String.format("%d %s identified in this pull-request.", count, s),
                            String.format("This PR contains %d %s, therefore only a repository admin may merge this pull request.", count, s));
                }
            }
        }
        // If an exception occurs performing this merge check, don't block the pull request from being merged - just log that an error occurred.
        catch (ScanIncompleteException e) {
            log.error("ERROR: Attempted to retrieve secret scan results, but a scan has not been started yet!");
        }
        catch (Exception e) {
            log.error("ERROR: Failed to check run HasSecretMergeCheck. Unknown Exception Occurred:" + e.toString()); //TODO: TRACEBACK
        }

        return RepositoryHookResult.accepted();
    }
}



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

/**
 * Merge-Check for an open pull-request to check whether any changed line contains a potential secret.
 * If one or more secrets if found, the merge will be blocked unless the user has repository admin permission.
 */
@Component("hasSecretMergeCheck")
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

        final PullRequest pullRequest = request.getPullRequest();
        final Repository repository = pullRequest.getToRef().getRepository();

        // TODO: Check if this applies to all pushes
        // TODO: settings is enabled else exit now

        PullRequestSecretScanner scannedPullRequest = new PullRequestSecretScanner(pullRequestService).scan(pullRequest);

        if (scannedPullRequest.countFoundSecrets() > 0) {
            if (!permissionService.hasRepositoryPermission(repository, Permission.REPO_ADMIN)) {
                String summaryMsg = i18nService.getMessage("com.cyanoth.secretwarden.hassecret.requireadmin",
                        "Possible secret(s) identified in this pull-request. You must have repository admin permissions to merge this request.");
                return RepositoryHookResult.rejected(summaryMsg, summaryMsg);
            }
        }

        return RepositoryHookResult.accepted();
    }
}



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
            // TODO: load the matched secret rule set & pass it to PullRequestSecretScanner

            final PullRequest pullRequest = request.getPullRequest();
            final Repository repository = pullRequest.getToRef().getRepository();

            PullRequestSecretScanner scannedPullRequest = new PullRequestSecretScanner(pullRequestService).scan(pullRequest);

            int foundSecretCount = scannedPullRequest.countFoundSecrets();
            if (foundSecretCount > 0) {
                if (!permissionService.hasRepositoryPermission(repository, Permission.REPO_ADMIN)) {
                    String summaryMsg = i18nService.createKeyedMessage("com.cyanoth.secretwarden.hassecretmergecheck.foundsecrets", foundSecretCount).toString();
                    String instructionMsg = i18nService.getMessage("com.cyanoth.secretwarden.hassecretmergecheck.requireadmin");

                    return RepositoryHookResult.rejected(summaryMsg, instructionMsg);
                }
            }
        }
        // If an exception occurs performing this merge check, don't block the pull request from being merged - just log that an error occurred.
        catch (ScanIncompleteException e) {
            log.error(i18nService.getMessage("com.cyanoth.secretwarden.hassecretmergecheck.log.scanincomplete"));
        }
        catch (Exception e) {
            log.error(i18nService.createKeyedMessage("com.cyanoth.secretwarden.hassecretmergecheck.log.unknownexception", e.toString()).toString());
        }

        return RepositoryHookResult.accepted();
    }
}



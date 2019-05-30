package com.cyanoth.secretwarden.conditions;

import com.atlassian.bitbucket.hook.repository.RepositoryHook;
import com.atlassian.bitbucket.hook.repository.RepositoryHookService;
import com.atlassian.bitbucket.repository.Repository;
import com.atlassian.bitbucket.scope.RepositoryScope;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.web.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

/**
 * A custom condition to check whether or not the HasSecretMergeCheck hook is enabled on a repository.
 */
@Scanned
public class PullRequestMergeCheckEnabled implements Condition {
    private static final Logger log = LoggerFactory.getLogger(PullRequestMergeCheckEnabled.class);
    private static final String HOOK_KEY = "com.cyanoth.secretwarden:has-secret-pr-merge-check";
    private final RepositoryHookService repositoryHookService;

    @Inject
    public PullRequestMergeCheckEnabled(@ComponentImport final RepositoryHookService repositoryHookService) {
        this.repositoryHookService = repositoryHookService;
    }

    @Override
    public void init(final Map<String, String> context) throws PluginParseException {
        // No behaviour
    }

    /**
     * @param context Calling context (such as the repository in question)
     * @return True - the condition passes (hook enabled). False otherwise
     */
    @Override
    public boolean shouldDisplay(final Map<String, Object> context) {
        final Object obj = context.get("repository");
        if (!(obj instanceof Repository)) {
            return false;
        }

        RepositoryHook hook = repositoryHookService.getByKey(new RepositoryScope((Repository) obj), HOOK_KEY);
        return hook != null && hook.isEnabled();
    }
}
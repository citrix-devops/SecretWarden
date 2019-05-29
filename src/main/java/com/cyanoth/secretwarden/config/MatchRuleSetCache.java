package com.cyanoth.secretwarden.config;

import com.atlassian.cache.Cache;
import com.atlassian.cache.CacheFactory;
import com.atlassian.cache.CacheSettings;
import com.atlassian.cache.CacheSettingsBuilder;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.cyanoth.secretwarden.RuleSetLoadException;
import com.cyanoth.secretwarden.collections.MatchRuleSet;
import com.cyanoth.secretwarden.structures.MatchRule;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * An exposed component uses the Atlassian Cache API to cache in memory a single instance of MatchRuleSet (identified by the key: RULESET_KEY)
 * With this, we can ensure that the loaded ruleset is replicated across all nodes in the cluster even after it has been updated by configuration changes
 * and remains in memory, so we don't have to fetch the rule every time from settings.
 *
 * [1] https://docs.atlassian.com/atlassian-cache-api/2.2.0/atlassian-cache-api/apidocs/com/atlassian/cache/CacheFactory.html
 */
@Component
public class MatchRuleSetCache {
    private static final Logger log = LoggerFactory.getLogger(MatchRuleSetCache.class);
    private final String CACHE_NAME = "com.cyanoth.secretwarden:MatchRuleSetCache";
    private final String RULESET_KEY = "MatchSecretRuleSet";
    private final MatchRuleSettings matchRuleSettings;
    private final CacheFactory cacheFactory;
    private final CacheSettings cacheSettings;
    private Cache<String, MatchRuleSet> _matchRuleSet = null; // Always Use cache() for access, even inner class

    @Autowired
    public MatchRuleSetCache(@ComponentImport CacheFactory cacheFactory,
                             MatchRuleSettings matchRuleSettings) {
        this.cacheFactory = cacheFactory;
        this.cacheSettings = new CacheSettingsBuilder().remote().replicateViaCopy().build();
        this.matchRuleSettings = matchRuleSettings;
    }

    private Cache<String, MatchRuleSet> cache() {
        synchronized (this) {
            if (this._matchRuleSet == null) {
                this._matchRuleSet = this.cacheFactory.getCache(CACHE_NAME, null, cacheSettings);
                try {
                    reloadRuleSet();
                    log.debug("SecretWarden: A new cache object has been initialised for MatchRuleSet!");
                }
                catch (RuleSetLoadException e) {
                    log.error("Failed to load SecretWarden RuleSet! RuleSet may be empty...");

                }
            }
            return this._matchRuleSet;
        }
    }

    /**
     * @return MatchRuleSet A collection of match secret rules loaded previous and stored in the cache
     */
    public MatchRuleSet getRuleSet() {
        return cache().get(RULESET_KEY);
    }

    /**
     * Reloads the ruleset from default plugin & user configuration rules. Incase of failure, the old ruleset is kept.
     */
    public void reloadRuleSet() throws RuleSetLoadException {
        log.info("Reloading SecretWarden MatchRuleset");
        try {

            // Intentionally load into a temporary local variable, incase an error occurs, the cache doesn't get replaced
            MatchRuleSet ruleSet = new MatchRuleSet();
            ruleSet.putAllRules(getDefaultRuleSet());
            ruleSet.putAllRules(getCustomRuleSet());

            cache().removeAll();
            cache().put(RULESET_KEY, ruleSet);

            log.info(String.format("SecretWarden ruleset reloaded successfully and contains: %d rules", cache().get(RULESET_KEY).count()));
        }
        catch (RuleSetLoadException e) {
            log.error(String.format("Failed to reload SecretWarden ruleset.\nAn exception has occurred: %s", e.getMessage()));
            throw e;
        }
    }

    /**
     * Load the built-in ruleset. Any changes the user has made (in settings) will override the values in the file
     * @return Set of secret rules from the plugin resources folder.
     * @throws RuleSetLoadException Exception which meant the default ruleset could not be loaded.
     */
    private Set<MatchRule> getDefaultRuleSet() throws RuleSetLoadException {
        try {
            log.debug("Loading the default ruleset");
            final Set<MatchRule> ruleCollector = new HashSet<>();

            final InputStream inFile = MatchRuleSet.class.getResourceAsStream("/defaults/secret_ruleset.json");
            final JsonArray rules = new JsonParser()
                        .parse(new InputStreamReader(inFile, StandardCharsets.UTF_8))
                        .getAsJsonObject().getAsJsonArray("rules");

            for (JsonElement rule : rules ) {
                final JsonObject ruleObj = rule.getAsJsonObject();
                try {
                    int ruleNumber = ruleObj.get("rule_number").getAsInt();
                    ruleCollector.add(new MatchRule(ruleNumber,
                            matchRuleSettings.getRuleNameOrDefault(ruleNumber, ruleObj.get("friendly_name").getAsString()),
                            matchRuleSettings.getRulePatternOrDefault(ruleNumber, ruleObj.get("regex_pattern").getAsString()),
                            matchRuleSettings.getRuleEnabledOrDefault(ruleNumber, true)));
                }
                catch (Exception e) {
                    log.warn("An exception occurred trying to load a default rule: Exception", e);
                }
            }
            log.debug(String.format("The default ruleset was loaded successfully and contains %d rules", ruleCollector.size()));
            return ruleCollector;
        }
        catch (Exception e) {
            throw new RuleSetLoadException("DefaultRuleSet", e);
        }
    }

    private Set<MatchRule> getCustomRuleSet() throws RuleSetLoadException {
        // Not Yet Implemented
        return new HashSet<>();
    }
}

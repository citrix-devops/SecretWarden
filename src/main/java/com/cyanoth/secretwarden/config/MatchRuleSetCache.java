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
            ruleSet.putAllRules(getCustomRuleSet(ruleSet));

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

    /**
     * Loads rules which are NOT in the plugin default rule files, these are rules stored in plugin settings added
     * by the user.
     * @param currentLoadedRuleset Previously loaded rules (i.e default ruleset) so they are skipped and not loaded again
     * @return Set of User added rules
     * @throws RuleSetLoadException Exception which meant the custom ruleset could not be loaded.
     */
    private Set<MatchRule> getCustomRuleSet(MatchRuleSet currentLoadedRuleset) throws RuleSetLoadException {
        try {
            log.debug("Loading the user custom ruleset");
            final Set<MatchRule> ruleCollector = new HashSet<>();

            int ruleNumber = 1;

            while (true) {
                if (currentLoadedRuleset.getRule(ruleNumber) != null) {
                    log.debug(String.format("Rule # %d is already loaded (default?)", ruleNumber));
                } else {
                    log.debug(String.format("Rule # %d is not loaded, checking in the settings", ruleNumber));

                    if (matchRuleSettings.getRuleNameOrDefault(ruleNumber, null) != null) {
                        try {
                            MatchRule incomingRule = new MatchRule(
                                    ruleNumber,
                                    matchRuleSettings.getRuleNameOrDefault(ruleNumber, "unknown"),
                                    matchRuleSettings.getRulePatternOrDefault(ruleNumber, ""),
                                    matchRuleSettings.getRuleEnabledOrDefault(ruleNumber, false));

                            ruleCollector.add(incomingRule);
                        }
                        catch (Exception e) {
                            log.warn("An exception occurred trying to load a default rule: Exception", e);
                        }
                    }
                    else {
                        break;
                    }
                }
                ruleNumber++;
            }
            return ruleCollector;
        }
        catch (Exception e) {
            throw new RuleSetLoadException("CustomRuleSet", e);
        }
    }

    public boolean createNewRule(String ruleName, String rulePattern, Boolean ruleEnabled) {
        synchronized (this) {
            int newRuleNumber = getFirstNonExistentRuleNumber();

            log.debug(String.format("Creating new rule: # %d, Name: %s Pattern: %s Enabled: %s",
                    newRuleNumber, ruleName, rulePattern, ruleEnabled));

            try {
                // The validate function is also called in set functions too but this duplicated call is
                // necessary for creating rules so to 'ensure' all properties can be created without rollback.
                matchRuleSettings.validateRuleName(ruleName);
                matchRuleSettings.validateRulePattern(rulePattern);

                matchRuleSettings.setRuleName(newRuleNumber, ruleName);
                matchRuleSettings.setRulePattern(newRuleNumber, rulePattern);
                matchRuleSettings.setRuleEnabled(newRuleNumber, ruleEnabled);
                return true;
            } catch (IllegalArgumentException e) {
                log.error("Failed to set create rule due to validation error: " + e.getMessage());
            }
            return false;
        }
    }


    /**
     * Find the first rule number that does not exist in plugin settings.
     * @return Integer. The first instance of a rule number that does not exist.
     */
    private int getFirstNonExistentRuleNumber() {
        int nextRuleNumber = 1;

        while (true) {
            if (matchRuleSettings.getRuleEnabledOrDefault(nextRuleNumber, null) == null) {
                log.debug(String.format("Rule number: %d does not exist in settings (may be default rule).", nextRuleNumber));

                // Default rules may not exist in settings if the user has changed them, so check cache.
                if (getRuleSet().getRule(nextRuleNumber) == null) {
                    log.debug(String.format("Rule number: %d does not exist in cache (may be default rule).", nextRuleNumber));
                    return nextRuleNumber;
                }
            }

            log.debug(String.format("Rule number: %d already exists.", nextRuleNumber));
            nextRuleNumber++;
        }
    }
}

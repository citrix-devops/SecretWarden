package com.cyanoth.secretwarden.config;

import com.cyanoth.secretwarden.structures.MatchRule;
import com.cyanoth.secretwarden.RuleSetLoadException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * A collection of match secret rules (regex expression & identifier) that find secrets in the source code.
 * We implement Serializable to be cluster-safe. Ensuring that incase the ruleset is reloaded (config change) - every node will have the same config.
 * This class was originally static, which would have worked fine for Bitbucket Server but not Data Center.
 */
public final class MatchRuleSet implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(MatchRuleSet.class);
    private Set<MatchRule> ruleSet = new HashSet<>();

    @NotNull
    public Set<MatchRule> getRuleSet() {
        return ruleSet;
    }

    /**
     * Reload the loaded ruleset.
     * @return True: Reload of ruleset was successful. False otherwise. Note: On false the previous loaded ruleset will NOT have changed
     */
    public boolean reloadRuleSet() {
        log.info("Reloading SecretWarden Ruleset");
        try {
            // Don't replace the current loaded ruleset until everything has loaded successfully
            final Set<MatchRule> loadedRuleSet = new HashSet<>();
            loadedRuleSet.addAll(getDefaultRuleSet());
            loadedRuleSet.addAll(getCustomRuleSet());

            ruleSet = loadedRuleSet;
            log.info(String.format("SecretWarden ruleset reloaded successfully and contains: %d rules", ruleSet.size()));
            return true;

        }
        catch (RuleSetLoadException e) {
            log.error(String.format("Failed to reload SecretWarden ruleset.\nAn exception has occurred: %s", e.getMessage()));
            // Do not replace the current ruleset on error.
            return false;
        }
    }

    /**
     * @return Set of secret rules from the plugin resources folder.
     * @throws RuleSetLoadException Any exception which meant the default ruleset could not be loaded in its entirety
     */
    private Set<MatchRule> getDefaultRuleSet() throws RuleSetLoadException {
        try {
            log.debug("Loading default ruleset");
            final Set<MatchRule> ruleCollector = new HashSet<>();

            final InputStream inFile = MatchRuleSet.class.getResourceAsStream("/defaults/secret_ruleset.json");
            final JsonArray rules = new JsonParser()
                        .parse(new InputStreamReader(inFile, StandardCharsets.UTF_8))
                        .getAsJsonObject().getAsJsonArray("rules");

            for (JsonElement rule : rules ) {
                final JsonObject ruleObj = rule.getAsJsonObject();

                try {
                    if (!ruleObj.get("enabled").getAsBoolean()) //TODO: Check if this in config
                        continue;

                    ruleCollector.add(new MatchRule(
                            ruleObj.get("rule_identifier").getAsString(),
                            ruleObj.get("friendly_name").getAsString(),
                            ruleObj.get("regex_pattern").getAsString()));
                }
                catch (Exception e) { //TODO: Improve me!
                    log.warn("Failed to load a default rule! Exception" + e.toString());
                }
            }
            log.debug(String.format("Finished loading default ruleset. Contains %d rules", ruleCollector.size()));
            return ruleCollector;
        }
        catch (Exception e) {
            throw new RuleSetLoadException("DefaultRuleSet", e.toString());
        }
    }

    private Set<MatchRule> getCustomRuleSet() throws RuleSetLoadException {
        // Not Yet Implemented
        return new HashSet<>();
    }
}

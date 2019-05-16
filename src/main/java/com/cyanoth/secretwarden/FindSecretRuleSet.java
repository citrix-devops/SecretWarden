package com.cyanoth.secretwarden;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Collection of Secret Rules and methods to reload the ruleset.
 * Class is static and ruleset should only be loaded once on plugin load or configuration change.
 */
public final class FindSecretRuleSet {
    private static final Logger log = LoggerFactory.getLogger(FindSecretRuleSet.class);
    private static Set<FindSecretRule> ruleSet = new HashSet<>();

    private FindSecretRuleSet() {} // Prevent Instantiation

    /**
     * @return Collection of loaded Secret Rules
     */
    @NotNull
    public static Set<FindSecretRule> getruleSet() {
        return ruleSet;
    }

    /**
     * Reload the loaded ruleset.
     * @return True: Reload of ruleset was successful, false otherwise.
     */
    public static boolean reloadRuleSet() {
        log.info("Reloading SecretWarden Ruleset");
        try {
            // ruleSet is globally static! Be safe - don't replace the current ruleset until everything is loaded successfully
            Set<FindSecretRule> loadedRuleSet = new HashSet<>();
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
    private static Set<FindSecretRule> getDefaultRuleSet() throws RuleSetLoadException {
        try {
            log.debug("Loading default ruleset");
            Set<FindSecretRule> ruleCollector = new HashSet<>();

            InputStream inFile = FindSecretRuleSet.class.getResourceAsStream("/defaults/secret_ruleset.json");
            JsonArray rules = new JsonParser()
                    .parse(new InputStreamReader(inFile, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonArray("rules");

            for (JsonElement rule : rules ) {
                JsonObject ruleObj = rule.getAsJsonObject();

                try {
                    if (!ruleObj.get("enabled").getAsBoolean())
                        continue;

                    ruleCollector.add(new FindSecretRule(
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

    private static Set<FindSecretRule> getCustomRuleSet() throws RuleSetLoadException {
        // Not Yet Implemented
        return new HashSet<>();
    }
}

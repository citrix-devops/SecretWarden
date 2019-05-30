package com.cyanoth.secretwarden.config;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * Methods to get, set and validate MatchRule properties from SETTINGS (i.e outside the cache)
 */
@Component
public class MatchRuleSettings {
    private static final Logger log = LoggerFactory.getLogger(MatchRuleSettings.class);
    private final PluginSettings pluginSettings;
    private final String SETTINGS_NAMESPACE = "com.cyanoth.secretwarden.settings"; // !! DO NOT CHANGE ME EVER (EVER) !!

    private final String KEY_RULE_PREFIX=".rule";
    private final String KEY_APPENDIX_NAME = "_name";
    private final String KEY_APPENDIX_PATTERN = "_pattern";
    private final String KEY_APPENDIX_ENABLED = "_enabled";

    private final int MIN_STRING_CHARACTERS = 3;
    private final int MAX_RULENAME_CHARACTERS = 100;

    private final int MAX_RULEPATTERN_CHARACTERS = 9000;
    // Technically, the max value here can be 99000
    // [1] https://docs.atlassian.com/DAC/javadoc/sal/2.6/reference/com/atlassian/sal/api/pluginsettings/PluginSettings.html

    @Autowired
    public MatchRuleSettings(@ComponentImport final PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettings = pluginSettingsFactory.createSettingsForKey(SETTINGS_NAMESPACE);
    }

    /**
     * Updates the rule name in plugin settings
     * @param ruleNumber The unique rule number to update the value of
     * @param ruleName The new friendly name of the rule
     * @return True if the rule name is updated. False otherwise
     */
    public boolean setRuleName(@NotNull int ruleNumber, @NotNull String ruleName) {
        try {
            validateRuleName(ruleName);
            String key = getRuleKeyName(ruleNumber, KEY_APPENDIX_NAME);
            pluginSettings.put(key, ruleName);
            log.debug(String.format("Set rule name for Rule #: %d Key: %s Value: %s", ruleNumber, key, ruleName));
            return true;
        }
        catch (IllegalArgumentException e) {
            log.error("Failed to set rule name due to validation error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update the pattern of an existing rule in plugin settings
     * @param ruleNumber The unique rule identifier number to update
     * @param rulePattern The regular expression pattern (String) to update to
     * @return True if successful, false otherwise.
     * @throws IllegalArgumentException Update pattern to did not pass validation
     */
    public boolean setRulePattern(@NotNull int ruleNumber, @NotNull String rulePattern) throws IllegalArgumentException {
        try {
            validateRulePattern(rulePattern);
            String key = getRuleKeyName(ruleNumber, KEY_APPENDIX_PATTERN);
            pluginSettings.put(key, rulePattern);
            log.debug(String.format("Set rule pattern for Rule #: %d Key: %s Value: %s", ruleNumber, key, rulePattern));
            return true;
        }
        catch(IllegalArgumentException e){
            log.error("Failed to set rule pattern due to validation error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update whether or not a rule is enabled (used for scanning)
     * @param ruleNumber The unique rule identifier number to update
     * @param ruleEnabled True - the rule should be used. False otherwise
     * @return True, rule enabled was changed successfully.
     */
    public boolean setRuleEnabled(@NotNull int ruleNumber, @NotNull Boolean ruleEnabled) {
        String key = getRuleKeyName(ruleNumber, KEY_APPENDIX_ENABLED);
        pluginSettings.put(key, ruleEnabled.toString());
        log.debug(String.format("Set rule enabled for Rule #: %d Key: %s Value: %s", ruleNumber, key, ruleEnabled.toString()));
        return true;
    }

    /**
     * Ensure that the change to rulename passes validation.
     * @param ruleName The name of the rule to validate
     * @throws IllegalArgumentException Rule name did not pass validation. Includes explanation why
     */
    public void validateRuleName(String ruleName) throws IllegalArgumentException {
        if (ruleName.length() <= MIN_STRING_CHARACTERS || ruleName.length() > MAX_RULENAME_CHARACTERS)
            throw new IllegalArgumentException(String.format("Rule name length must be greater than %d characters & smaller than %d characters.",
                    MIN_STRING_CHARACTERS, MAX_RULENAME_CHARACTERS));
    }

    /**
     * Ensure that the change to the rule pattern passes validation.
     * @param rulePattern The pattern of the rule to validate
     * @throws IllegalArgumentException Rule pattern did not pass validation. Includes explanation why.
     */
    public void validateRulePattern(String rulePattern) throws IllegalArgumentException {
         if (rulePattern.length() <= MIN_STRING_CHARACTERS || rulePattern.length() > MAX_RULEPATTERN_CHARACTERS)
            throw new IllegalArgumentException(String.format("Rule pattern length must be greater than %d characters & smaller than %d characters.",
                    MIN_STRING_CHARACTERS, MAX_RULENAME_CHARACTERS));

        // Naively, we trust that the user has put a valid regex expression. Should consider changing that.
    }

    /**
     * Get the rule name of a rule identified by the number in settings. Returns a default value if rule is not found.
     * @param ruleNumber The rule number to get the name of
     * @param defaultValue The value to return if the rule was not found
     * @return Value of the rule name or defaultValue if not found
     */
    public String getRuleNameOrDefault(@NotNull int ruleNumber, @Nullable String defaultValue) {
        String ruleName = (String) pluginSettings.get(getRuleKeyName(ruleNumber, KEY_APPENDIX_NAME));
        return (ruleName == null) ? defaultValue : ruleName;
    }

    /**
     * Get the rule pattern of a rule identified by the number in settings. Returns a default value if rule is not found.
     * @param ruleNumber The rule number to get the name of
     * @param defaultValue The value to return if the rule was not found
     * @return Value of the rule name or defaultValue if not found
     */
    public String getRulePatternOrDefault(@NotNull int ruleNumber, @Nullable String defaultValue) {
        String rulePattern = (String) pluginSettings.get(getRuleKeyName(ruleNumber, KEY_APPENDIX_PATTERN));
        return (rulePattern == null) ? defaultValue : rulePattern;
    }

    /**
     * Get whether or not a rule is enabled in settings. Returns a default value if rule is not found.
     * @param ruleNumber The rule number to get the name of
     * @param defaultValue The value to return if the rule was not found
     * @return Value of the rule name or defaultValue if not found
     */
    public Boolean getRuleEnabledOrDefault(@NotNull int ruleNumber, @Nullable Boolean defaultValue) {
        String ruleEnabled = (String) pluginSettings.get(getRuleKeyName(ruleNumber, KEY_APPENDIX_ENABLED));
        return (ruleEnabled == null) ? defaultValue : Boolean.valueOf(ruleEnabled);
    }

    /**
     * Internal function to get a unique key identifier in plugin settings
     * @param ruleNumber The rule number to get key for
     * @param appendix Append a hardcoded string to identify the type of setting.
     * @return String that is a unique key for the plugin setting rule.
     */
    private String getRuleKeyName(int ruleNumber, String appendix) {
        return SETTINGS_NAMESPACE + KEY_RULE_PREFIX + ruleNumber + appendix;
    }


}

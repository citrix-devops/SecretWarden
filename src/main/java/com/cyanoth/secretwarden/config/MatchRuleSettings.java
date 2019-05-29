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

    public boolean createRule(String ruleName, String rulePattern, Boolean ruleEnabled) throws IllegalArgumentException {
        synchronized (this) {
            int newRuleNumber = getFirstNonExistentRuleNumber();

            log.debug(String.format("Creating new rule: #%d, Name: %s Pattern: %s Enabled: %s"),
                    newRuleNumber, ruleName, rulePattern, ruleEnabled);

            try {
                // The validate function is also called in set functions too but this duplicated call is
                // necessary for creating rules so to 'ensure' all properties can be created without rollback.
                validateRuleName(ruleName);
                validateRulePattern(rulePattern);

                setRuleName(newRuleNumber, ruleName);
                setRulePattern(newRuleNumber, rulePattern);
                setRuleEnabled(newRuleNumber, ruleEnabled);
                return true;
            }
            catch (IllegalArgumentException e){
                log.error("Failed to set create rule due to validation error: " + e.getMessage());
            }
            return false;
        }
    }

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

    public boolean setRuleEnabled(@NotNull int ruleNumber, @NotNull Boolean ruleEnabled) {
        String key = getRuleKeyName(ruleNumber, KEY_APPENDIX_ENABLED);
        pluginSettings.put(key, ruleEnabled);
        log.debug(String.format("Set rule pattern for Rule #: %d Key: %s Value: %s", ruleNumber, key, ruleEnabled.toString()));
        return true;
    }

    private void validateRuleName(String ruleName) throws IllegalArgumentException {
        if (ruleName.length() <= MIN_STRING_CHARACTERS || ruleName.length() > MAX_RULENAME_CHARACTERS)
            throw new IllegalArgumentException(String.format("Rule name length must be greater than %d characters & smaller than %d characters.",
                    MIN_STRING_CHARACTERS, MAX_RULENAME_CHARACTERS));

        if (!ruleName.matches("^[a-zA-Z0-9_-]*$"))
            throw new IllegalArgumentException("Rulename must only contain alphanumeric characters & underscores");
    }

    private void validateRulePattern(String rulePattern) throws IllegalArgumentException {
         if (rulePattern.length() <= MIN_STRING_CHARACTERS || rulePattern.length() > MAX_RULEPATTERN_CHARACTERS)
            throw new IllegalArgumentException(String.format("Rule pattern length must be greater than %d characters & smaller than %d characters.",
                    MIN_STRING_CHARACTERS, MAX_RULENAME_CHARACTERS));

        // Naively, we trust that the user has put a valid regex expression. Should consider changing that.
    }

    public String getRuleNameOrDefault(@NotNull int ruleNumber, @Nullable String defaultValue) {
        String ruleName = (String) pluginSettings.get(getRuleKeyName(ruleNumber, KEY_APPENDIX_NAME));
        return (ruleName == null) ? defaultValue : ruleName;
    }

    public String getRulePatternOrDefault(@NotNull int ruleNumber, @Nullable String defaultValue) {
        String rulePattern = (String) pluginSettings.get(getRuleKeyName(ruleNumber, KEY_APPENDIX_PATTERN));
        return (rulePattern == null) ? defaultValue : rulePattern;
    }

    public Boolean getRuleEnabledOrDefault(@NotNull int ruleNumber, @Nullable Boolean defaultValue) {
        Boolean ruleEnabled = (Boolean) pluginSettings.get(getRuleKeyName(ruleNumber, KEY_APPENDIX_ENABLED));
        return (ruleEnabled == null) ? defaultValue : ruleEnabled;
    }


    private String getRuleKeyName(int ruleNumber, String appendix) {
        return SETTINGS_NAMESPACE + KEY_RULE_PREFIX + ruleNumber + appendix;
    }

    private int getFirstNonExistentRuleNumber() {
        int nextRuleNumber = 0;

        return 123;
    }
}

package com.cyanoth.secretwarden.config;

import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

@Scanned
@Component
public class MatchRuleSettings {
    private final String SETTINGS_NAMESPACE = "com.cyanoth.secretwarden.settings"; // !! DO NOT CHANGE ME EVER !!

    private final String KEY_RULE_PREFIX=".rule";
    private final String KEY_APPENDIX_NAME = "_name";
    private final String KEY_APPENDIX_PATTERN = "_pattern";
    private final String KEY_APPENDIX_ENABLED = "_enabled";

    private final PluginSettings pluginSettings;

    @Autowired
    public MatchRuleSettings(@ComponentImport final PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettings = pluginSettingsFactory.createSettingsForKey(SETTINGS_NAMESPACE);

    }

    // TODO: Synchronised
    public void createRule(String ruleName, String rulePattern, Boolean ruleEnabled) {
        // TODO get latest rule number

        int randomNumber = 100; // replace me!
        setRuleName(randomNumber, ruleName);
        setRulePattern(randomNumber, rulePattern);
        setRuleEnabled(randomNumber, ruleEnabled);
    }

    public void setRuleName(int ruleNumber, String ruleName) {
        // TODO: Additional Validation

        pluginSettings.put(getRuleKeyName(ruleNumber, KEY_APPENDIX_NAME), ruleName);

    }

    public void setRulePattern(int ruleNumber, String rulePattern) {
        // TODO: Additional Validation

        pluginSettings.put(getRuleKeyName(ruleNumber, KEY_APPENDIX_PATTERN), rulePattern);
    }

    public void setRuleEnabled(int ruleNumber, Boolean ruleEnabled) {
        // TODO: Additional Validation

        pluginSettings.put(getRuleKeyName(ruleNumber, KEY_APPENDIX_ENABLED), ruleEnabled);

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




}

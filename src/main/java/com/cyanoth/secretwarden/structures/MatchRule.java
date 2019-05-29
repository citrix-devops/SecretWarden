package com.cyanoth.secretwarden.structures;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * A match rule is metadata onto of a regular expression, such as a friendly name that would appear to a user.
 */
public class MatchRule implements Serializable {
    private final int ruleNumber;
    private final String friendlyName;
    private final String regexPattern; // Used on configuration page. Use the pre-compiled compiledRegexPattern elsewhere
    private final Pattern compiledRegexPattern;
    private final Boolean enabled;

    public MatchRule(int ruleNumber, String friendlyName, String regexPattern, Boolean enabled) {
        this.ruleNumber = ruleNumber;
        this.friendlyName = friendlyName;
        this.regexPattern = regexPattern;
        this.compiledRegexPattern = Pattern.compile(regexPattern);
        this.enabled = true;
    }

    @NotNull
    public String getFriendlyName() {
        return friendlyName;
    }

    @NotNull
    public Pattern getCompiledRegexPattern() {
        return compiledRegexPattern;
    }

    @NotNull
    public int getRuleNumber() {
        return ruleNumber;
    }
}

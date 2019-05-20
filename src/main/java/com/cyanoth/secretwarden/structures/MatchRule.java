package com.cyanoth.secretwarden.structures;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * A match rule is metadata onto of a regular expression, such as a friendly name that would appear to a user.
 */
public class MatchRule implements Serializable {
    private final String identifier;
    private final String friendlyName;
    private final Pattern regexPattern;
    private final Boolean enabled;

    public MatchRule(String identifier, String friendlyName, String regexPattern, Boolean enabled) {
        this.identifier = identifier;
        this.friendlyName = friendlyName;
        this.regexPattern = Pattern.compile(regexPattern);
        this.enabled = true;
    }

    @NotNull
    public String getIdentifier() {
        return identifier;
    }

    @NotNull
    public String getFriendlyName() {
        return friendlyName;
    }

    @NotNull
    public Pattern getRegexPattern() {
        return regexPattern;
    }
}

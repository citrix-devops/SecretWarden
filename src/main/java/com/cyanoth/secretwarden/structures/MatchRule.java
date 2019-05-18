package com.cyanoth.secretwarden.structures;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.regex.Pattern;

public class MatchRule implements Serializable {
    private final String identifier;
    private final String friendlyName;
    private final Pattern regexPattern;

    public MatchRule(String identifier, String friendlyName, String regexPattern) {
        this.identifier = identifier;
        this.friendlyName = friendlyName;
        this.regexPattern = Pattern.compile(regexPattern);
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

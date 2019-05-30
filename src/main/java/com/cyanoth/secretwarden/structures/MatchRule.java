package com.cyanoth.secretwarden.structures;

import org.codehaus.jackson.annotate.JsonProperty;

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
    private transient Pattern compiledRegexPattern = null;
    private final Boolean enabled;

    public MatchRule(@JsonProperty("ruleNumber") int ruleNumber,
                     @JsonProperty("friendlyName") String friendlyName,
                     @JsonProperty("regexPattern") String regexPattern,
                     @JsonProperty("enabled") Boolean enabled) {
        this.ruleNumber = ruleNumber;
        this.friendlyName = friendlyName;
        this.regexPattern = regexPattern;
        this.enabled = enabled;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public Pattern getCompiledRegexPattern() {
        if (compiledRegexPattern == null) {
            compiledRegexPattern = Pattern.compile(regexPattern);
        }

        return compiledRegexPattern;
    }

    public Integer getRuleNumber() {
        return ruleNumber;
    }

    public Boolean getIsEnabled() {
        return enabled;
    }
}

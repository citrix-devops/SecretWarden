package com.cyanoth.secretwarden.structures;

import org.codehaus.jackson.annotate.JsonProperty;
import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * A match rule is metadata onto of a regular expression, such as a friendly name that would appear to a user.
 */
public class MatchRule implements Serializable {
    private final int ruleNumber;
    private final String friendlyName;
    private final String regexPattern; // Used only on configuration page. Use the pre-compiled compiledRegexPattern elsewhere
    private transient Pattern compiledRegexPattern;
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
        // Incase THIS object gets deserialized (constructor isn't called). The compiled pattern might not be initialised.
        if (compiledRegexPattern == null)
            compiledRegexPattern = Pattern.compile(regexPattern, Pattern.CASE_INSENSITIVE);

        return compiledRegexPattern;
    }

    public Integer getRuleNumber() {
        return ruleNumber;
    }

    public Boolean getIsEnabled() {
        return enabled;
    }

    /**
     * Check whether a string matches this rule
     * @param str String to test rule against
     * @return True. String matches rule. False otherwise.
     */
    public Boolean checkMatch(String str) {
        return this.getCompiledRegexPattern().matcher(str).find();
    }

}

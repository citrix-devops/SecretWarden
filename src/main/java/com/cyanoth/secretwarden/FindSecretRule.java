package com.cyanoth.secretwarden;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.validation.constraints.NotNull;
import java.util.regex.Pattern;

/**
 * Properties on a single Secret Rule.
 */
public class FindSecretRule {
    private static final Logger log = LoggerFactory.getLogger(FindSecretRuleSet.class);

    private final String identifier;
    private final String friendlyName;
    private final Pattern regexPattern;

    FindSecretRule(String identifier, String friendlyName, String regexPattern) {
        this.identifier = identifier;
        this.friendlyName = friendlyName;
        this.regexPattern = Pattern.compile(regexPattern);
        log.debug(String.format("New SecretWarden Rule Initialised. Name: %s | Identifier: %s",
                this.identifier, this.friendlyName));
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

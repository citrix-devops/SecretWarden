package com.cyanoth.secretwarden;

public class RuleSetLoadException extends Exception {

    public RuleSetLoadException(String rulesetName, String cause) {
        super(String.format("Exception occurred trying to load the ruleset: %s. Message: %s", rulesetName, cause));
    }

}

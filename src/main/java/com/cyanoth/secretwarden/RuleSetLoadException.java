package com.cyanoth.secretwarden;

public class RuleSetLoadException extends Exception {

    public RuleSetLoadException(String rulesetName, Exception e) {
        super(String.format("Exception occurred trying to load the ruleset: %s", rulesetName), e);
    }

}

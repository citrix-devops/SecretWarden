package com.cyanoth.secretwarden;

class RuleSetLoadException extends Exception {

    RuleSetLoadException(String rulesetName, String cause) {
        super(String.format("Exception occurred trying to load the ruleset: %s. Message: %s", rulesetName, cause));
    }

}

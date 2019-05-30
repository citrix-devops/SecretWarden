package com.cyanoth.secretwarden.collections;

import com.cyanoth.secretwarden.structures.MatchRule;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

/**
 * A collection of MatchRule objects, that is, the name of a secret rule matching pattern & regular expression.
 * Must implement Serializable to be cluster-safe & ensuring that upon reload (config change) - every node will have the same ruleset.
 */
public class MatchRuleSet implements Serializable {

    private HashMap<Integer, MatchRule> matchRules = new HashMap<>();

    public void putRule(MatchRule rule) {
        matchRules.put(rule.getRuleNumber(), rule);
    }

    public void putAllRules(Collection<MatchRule> rules) {
        for (MatchRule rule : rules) {
            putRule(rule);
        }
    }

    @Nullable
    public MatchRule getRule(int ruleNumber) {
        return matchRules.get(ruleNumber);
    }

    public Collection<MatchRule> getAllRules() {
        return matchRules.values();
    }

    public int count() {
        return matchRules.size();
    }
}

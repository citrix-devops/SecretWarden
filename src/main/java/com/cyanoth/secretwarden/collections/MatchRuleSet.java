package com.cyanoth.secretwarden.collections;

import com.cyanoth.secretwarden.structures.MatchRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

/**
 * A collection of MatchRules (subsequently contains regex expression & identifier). These rules find matches in the source code.
 * Must implement Serializable to be cluster-safe & ensuring that upon reload (config change) - every node will have the same ruleset.
 */
public class MatchRuleSet implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(MatchRuleSet.class);

    private HashMap<String, MatchRule> matchRules = new HashMap<>();

    public void putRule(MatchRule rule) {
        matchRules.put(rule.getIdentifier(), rule);
    }

    public void putAllRules(Collection<MatchRule> rules) {
        for (MatchRule rule : rules) {
            putRule(rule);
        }
    }

    public MatchRule getRule(String identifier) {
        return matchRules.get(identifier);
    }

    public Collection<MatchRule> getAllRules() {
        return matchRules.values();
    }

    public int count() {
        return matchRules.size();
    }
}

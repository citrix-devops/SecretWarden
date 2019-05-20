package com.cyanoth.secretwarden;

import com.cyanoth.secretwarden.structures.MatchRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

/**
 * A collection of match secret rules (regex expression & identifier) that find secrets in the source code.
 * We implement Serializable to be cluster-safe. Ensuring that incase the ruleset is reloaded (config change) - every node will have the same config.
 * This class was originally static, which would have worked fine for Bitbucket Server but not Data Center.
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

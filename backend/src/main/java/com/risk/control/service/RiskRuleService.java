package com.risk.control.service;

import com.risk.control.entity.RiskRule;
import com.risk.control.store.DataStore;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RiskRuleService {
    private final DataStore dataStore = DataStore.getInstance();

    public List<RiskRule> getAllRules() {
        return dataStore.getAllRules();
    }

    public List<RiskRule> getEnabledRules() {
        return dataStore.getAllRules().stream()
                .filter(RiskRule::isEnabled)
                .sorted(Comparator.comparingInt(RiskRule::getPriority))
                .collect(Collectors.toList());
    }

    public RiskRule getRuleById(Long id) {
        return dataStore.getRiskRuleMap().get(id);
    }

    public RiskRule createRule(RiskRule rule) {
        Long id = dataStore.generateRuleId();
        rule.setId(id);
        dataStore.getRiskRuleMap().put(id, rule);
        return rule;
    }

    public RiskRule updateRule(RiskRule rule) {
        RiskRule existingRule = dataStore.getRiskRuleMap().get(rule.getId());
        if (existingRule != null) {
            dataStore.getRiskRuleMap().put(rule.getId(), rule);
            return rule;
        }
        return null;
    }

    public boolean deleteRule(Long id) {
        return dataStore.getRiskRuleMap().remove(id) != null;
    }

    public RiskRule toggleRule(Long id) {
        RiskRule rule = dataStore.getRiskRuleMap().get(id);
        if (rule != null) {
            rule.setEnabled(!rule.isEnabled());
            return rule;
        }
        return null;
    }
}

package com.risk.control.controller;

import com.risk.control.common.Result;
import com.risk.control.entity.RiskRule;
import com.risk.control.service.RiskRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
public class RiskRuleController {
    @Autowired
    private RiskRuleService riskRuleService;

    @GetMapping
    public Result<List<RiskRule>> getAllRules() {
        return Result.success(riskRuleService.getAllRules());
    }

    @GetMapping("/{id}")
    public Result<RiskRule> getRuleById(@PathVariable Long id) {
        RiskRule rule = riskRuleService.getRuleById(id);
        if (rule != null) {
            return Result.success(rule);
        }
        return Result.error("规则不存在");
    }

    @PostMapping
    public Result<RiskRule> createRule(@RequestBody RiskRule rule) {
        try {
            RiskRule createdRule = riskRuleService.createRule(rule);
            return Result.success(createdRule);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<RiskRule> updateRule(@PathVariable Long id, @RequestBody RiskRule rule) {
        rule.setId(id);
        RiskRule updatedRule = riskRuleService.updateRule(rule);
        if (updatedRule != null) {
            return Result.success(updatedRule);
        }
        return Result.error("规则不存在");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRule(@PathVariable Long id) {
        boolean deleted = riskRuleService.deleteRule(id);
        if (deleted) {
            return Result.success();
        }
        return Result.error("规则不存在");
    }

    @PostMapping("/{id}/toggle")
    public Result<RiskRule> toggleRule(@PathVariable Long id) {
        RiskRule rule = riskRuleService.toggleRule(id);
        if (rule != null) {
            return Result.success(rule);
        }
        return Result.error("规则不存在");
    }
}

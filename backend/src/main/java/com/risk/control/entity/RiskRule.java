package com.risk.control.entity;

import com.risk.control.enums.ActionType;
import com.risk.control.enums.RiskLevel;
import com.risk.control.enums.RuleType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RiskRule {
    private Long id;
    private String name;
    private RuleType ruleType;
    private Integer priority;
    private RiskLevel riskLevel;
    private ActionType actionType;
    private BigDecimal thresholdAmount;
    private Integer thresholdCount;
    private Integer timeWindowMinutes;
    private boolean enabled;
    private String description;
}

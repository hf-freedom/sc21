package com.risk.control.entity;

import com.risk.control.enums.ActionType;
import com.risk.control.enums.RiskLevel;
import com.risk.control.enums.RuleType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RuleHitRecord {
    private Long id;
    private Long transactionId;
    private String requestNo;
    private Long userId;
    private Long ruleId;
    private String ruleName;
    private RuleType ruleType;
    private RiskLevel riskLevel;
    private ActionType actionType;
    private String hitDetails;
    private LocalDateTime hitTime;
}

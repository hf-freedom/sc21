package com.risk.control.entity;

import com.risk.control.enums.RiskLevel;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    private RiskLevel riskLevel;
    private boolean blacklisted;
    private Integer dailyTransactionCount;
    private BigDecimal dailyTransactionAmount;
    private LocalDate lastTransactionDate;
    private Integer consecutiveHighRiskHits;
    private Integer consecutiveFailures;
    private BigDecimal balance;
    private BigDecimal frozenBalance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

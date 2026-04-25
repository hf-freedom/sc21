package com.risk.control.entity;

import com.risk.control.enums.TransactionStatus;
import com.risk.control.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Transaction {
    private Long id;
    private String requestNo;
    private Long userId;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String hitRules;
    private List<RuleHitRecord> hitRecords;
    private String reviewComment;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime updatedAt;
}

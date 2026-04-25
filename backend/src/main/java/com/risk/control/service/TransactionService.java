package com.risk.control.service;

import com.risk.control.entity.RiskRule;
import com.risk.control.entity.RuleHitRecord;
import com.risk.control.entity.Transaction;
import com.risk.control.entity.User;
import com.risk.control.enums.ActionType;
import com.risk.control.enums.RiskLevel;
import com.risk.control.enums.RuleType;
import com.risk.control.enums.TransactionStatus;
import com.risk.control.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final DataStore dataStore = DataStore.getInstance();

    @Autowired
    private UserService userService;

    @Autowired
    private RiskRuleService riskRuleService;

    public List<Transaction> getAllTransactions() {
        return dataStore.getAllTransactions();
    }

    public Transaction getTransactionById(Long id) {
        return dataStore.getTransactionMap().get(id);
    }

    public Transaction getTransactionByRequestNo(String requestNo) {
        return dataStore.getAllTransactions().stream()
                .filter(t -> requestNo.equals(t.getRequestNo()))
                .findFirst()
                .orElse(null);
    }

    public List<Transaction> getPendingReviewTransactions() {
        return dataStore.getAllTransactions().stream()
                .filter(t -> TransactionStatus.FROZEN.equals(t.getStatus()))
                .collect(Collectors.toList());
    }

    public Transaction initiateTransaction(Long userId, BigDecimal amount, String type) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("余额不足");
        }

        Transaction transaction = new Transaction();
        Long id = dataStore.generateTransactionId();
        transaction.setId(id);
        transaction.setRequestNo(generateRequestNo());
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setType(com.risk.control.enums.TransactionType.valueOf(type.toUpperCase()));
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        List<RuleHitRecord> hitRecords = evaluateRules(user, transaction);

        if (!hitRecords.isEmpty()) {
            transaction.setHitRecords(hitRecords);
            RuleHitRecord highestPriorityHit = hitRecords.stream()
                    .min(Comparator.comparingInt(h -> getRulePriority(h.getRuleId())))
                    .orElse(hitRecords.get(0));

            ActionType actionType = highestPriorityHit.getActionType();
            transaction.setStatus(mapActionToStatus(actionType));

            for (RuleHitRecord record : hitRecords) {
                record.setTransactionId(id);
                record.setRequestNo(transaction.getRequestNo());
                dataStore.getRuleHitRecordMap().put(record.getId(), record);
            }

            if (ActionType.FREEZE.equals(actionType)) {
                userService.freezeBalance(userId, amount);
            } else if (ActionType.REJECT.equals(actionType)) {
                userService.incrementConsecutiveFailures(userId);
            } else if (ActionType.BLACKLIST.equals(actionType)) {
                userService.addToBlacklist(userId);
                userService.incrementConsecutiveFailures(userId);
            }

            boolean hasHighRiskHit = hitRecords.stream()
                    .anyMatch(r -> RiskLevel.HIGH.equals(r.getRiskLevel()));
            
            if (hasHighRiskHit) {
                userService.incrementConsecutiveHighRiskHits(userId);
                User updatedUser = userService.getUserById(userId);
                if (updatedUser != null && updatedUser.getConsecutiveHighRiskHits() >= 2) {
                    userService.addToBlacklist(userId);
                }
            } else {
                userService.resetConsecutiveHighRiskHits(userId);
            }
        } else {
            transaction.setStatus(TransactionStatus.APPROVED);
            userService.deductBalance(userId, amount);
            userService.updateDailyTransactionStats(userId, amount);
            userService.resetConsecutiveFailures(userId);
            userService.resetConsecutiveHighRiskHits(userId);
            completeTransaction(transaction);
        }

        dataStore.getTransactionMap().put(id, transaction);
        return transaction;
    }

    private List<RuleHitRecord> evaluateRules(User user, Transaction transaction) {
        List<RuleHitRecord> hitRecords = new ArrayList<>();
        List<RiskRule> enabledRules = riskRuleService.getEnabledRules();

        for (RiskRule rule : enabledRules) {
            RuleHitRecord hitRecord = checkRule(rule, user, transaction);
            if (hitRecord != null) {
                hitRecords.add(hitRecord);
            }
        }

        return hitRecords;
    }

    private RuleHitRecord checkRule(RiskRule rule, User user, Transaction transaction) {
        boolean hit = false;
        String hitDetails = "";

        switch (rule.getRuleType()) {
            case BLACKLIST:
                hit = user.isBlacklisted();
                hitDetails = hit ? "用户在黑名单中" : "";
                break;

            case SINGLE_AMOUNT:
                hit = transaction.getAmount().compareTo(rule.getThresholdAmount()) >= 0;
                hitDetails = hit ? String.format("单笔金额 %.2f 超过阈值 %.2f",
                        transaction.getAmount(), rule.getThresholdAmount()) : "";
                break;

            case SHORT_TERM_FREQUENCY:
                LocalDateTime timeWindowStart = LocalDateTime.now().minusMinutes(rule.getTimeWindowMinutes());
                long recentTransactions = dataStore.getAllTransactions().stream()
                        .filter(t -> t.getUserId().equals(user.getId()))
                        .filter(t -> t.getCreatedAt().isAfter(timeWindowStart))
                        .count();
                hit = recentTransactions >= rule.getThresholdCount();
                hitDetails = hit ? String.format("过去 %d 分钟内交易 %d 笔，超过阈值 %d",
                        rule.getTimeWindowMinutes(), recentTransactions, rule.getThresholdCount()) : "";
                break;

            case DAILY_TOTAL_AMOUNT:
                BigDecimal dailyTotal = user.getDailyTransactionAmount().add(transaction.getAmount());
                hit = dailyTotal.compareTo(rule.getThresholdAmount()) >= 0;
                hitDetails = hit ? String.format("当日累计金额 %.2f 超过阈值 %.2f",
                        dailyTotal, rule.getThresholdAmount()) : "";
                break;

            case CONSECUTIVE_FAILURES:
                hit = user.getConsecutiveFailures() >= rule.getThresholdCount();
                hitDetails = hit ? String.format("连续失败 %d 次，超过阈值 %d",
                        user.getConsecutiveFailures(), rule.getThresholdCount()) : "";
                break;
        }

        if (hit) {
            RuleHitRecord record = new RuleHitRecord();
            record.setId(dataStore.generateRuleHitRecordId());
            record.setRuleId(rule.getId());
            record.setRuleName(rule.getName());
            record.setRuleType(rule.getRuleType());
            record.setRiskLevel(rule.getRiskLevel());
            record.setActionType(rule.getActionType());
            record.setUserId(user.getId());
            record.setHitDetails(hitDetails);
            record.setHitTime(LocalDateTime.now());
            return record;
        }

        return null;
    }

    private int getRulePriority(Long ruleId) {
        RiskRule rule = riskRuleService.getRuleById(ruleId);
        return rule != null ? rule.getPriority() : Integer.MAX_VALUE;
    }

    private TransactionStatus mapActionToStatus(ActionType actionType) {
        switch (actionType) {
            case FREEZE:
                return TransactionStatus.FROZEN;
            case REJECT:
                return TransactionStatus.REJECTED;
            case BLACKLIST:
                return TransactionStatus.BLACKLISTED;
            default:
                return TransactionStatus.APPROVED;
        }
    }

    public Transaction approveTransaction(Long transactionId, String comment) {
        Transaction transaction = dataStore.getTransactionMap().get(transactionId);
        if (transaction == null) {
            throw new RuntimeException("交易不存在");
        }

        if (!TransactionStatus.FROZEN.equals(transaction.getStatus())) {
            throw new RuntimeException("交易状态不支持审核");
        }

        transaction.setStatus(TransactionStatus.APPROVED);
        transaction.setReviewComment(comment);
        transaction.setReviewedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        userService.deductFrozenBalance(transaction.getUserId(), transaction.getAmount());
        userService.updateDailyTransactionStats(transaction.getUserId(), transaction.getAmount());
        userService.resetConsecutiveFailures(transaction.getUserId());
        userService.resetConsecutiveHighRiskHits(transaction.getUserId());

        completeTransaction(transaction);
        return transaction;
    }

    public Transaction rejectTransaction(Long transactionId, String comment) {
        Transaction transaction = dataStore.getTransactionMap().get(transactionId);
        if (transaction == null) {
            throw new RuntimeException("交易不存在");
        }

        if (!TransactionStatus.FROZEN.equals(transaction.getStatus())) {
            throw new RuntimeException("交易状态不支持审核");
        }

        transaction.setStatus(TransactionStatus.REJECTED);
        transaction.setReviewComment(comment);
        transaction.setReviewedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        userService.releaseFrozenBalance(transaction.getUserId(), transaction.getAmount());
        userService.incrementConsecutiveFailures(transaction.getUserId());
        return transaction;
    }

    private void completeTransaction(Transaction transaction) {
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setUpdatedAt(LocalDateTime.now());
    }

    private String generateRequestNo() {
        return "TXN" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public List<RuleHitRecord> getTransactionHitRecords(Long transactionId) {
        return dataStore.getAllRuleHitRecords().stream()
                .filter(r -> transactionId.equals(r.getTransactionId()))
                .collect(Collectors.toList());
    }
}

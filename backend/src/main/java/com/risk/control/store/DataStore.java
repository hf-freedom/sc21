package com.risk.control.store;

import com.risk.control.entity.RiskRule;
import com.risk.control.entity.RuleHitRecord;
import com.risk.control.entity.Transaction;
import com.risk.control.entity.User;
import com.risk.control.enums.ActionType;
import com.risk.control.enums.RiskLevel;
import com.risk.control.enums.RuleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DataStore {
    private static final DataStore INSTANCE = new DataStore();

    private final AtomicLong userIdGenerator = new AtomicLong(1);
    private final AtomicLong transactionIdGenerator = new AtomicLong(1);
    private final AtomicLong ruleIdGenerator = new AtomicLong(1);
    private final AtomicLong ruleHitRecordIdGenerator = new AtomicLong(1);

    private final Map<Long, User> userMap = new ConcurrentHashMap<>();
    private final Map<Long, Transaction> transactionMap = new ConcurrentHashMap<>();
    private final Map<Long, RiskRule> riskRuleMap = new ConcurrentHashMap<>();
    private final Map<Long, RuleHitRecord> ruleHitRecordMap = new ConcurrentHashMap<>();

    private DataStore() {
        initializeData();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    private void initializeData() {
        User user1 = new User();
        user1.setId(userIdGenerator.getAndIncrement());
        user1.setUsername("test_user_001");
        user1.setRiskLevel(RiskLevel.LOW);
        user1.setBlacklisted(false);
        user1.setDailyTransactionCount(0);
        user1.setDailyTransactionAmount(BigDecimal.ZERO);
        user1.setConsecutiveHighRiskHits(0);
        user1.setConsecutiveFailures(0);
        user1.setBalance(new BigDecimal("10000.00"));
        user1.setFrozenBalance(BigDecimal.ZERO);
        user1.setCreatedAt(LocalDateTime.now());
        user1.setUpdatedAt(LocalDateTime.now());
        userMap.put(user1.getId(), user1);

        User user2 = new User();
        user2.setId(userIdGenerator.getAndIncrement());
        user2.setUsername("test_user_002");
        user2.setRiskLevel(RiskLevel.MEDIUM);
        user2.setBlacklisted(false);
        user2.setDailyTransactionCount(0);
        user2.setDailyTransactionAmount(BigDecimal.ZERO);
        user2.setConsecutiveHighRiskHits(0);
        user2.setConsecutiveFailures(0);
        user2.setBalance(new BigDecimal("5000.00"));
        user2.setFrozenBalance(BigDecimal.ZERO);
        user2.setCreatedAt(LocalDateTime.now());
        user2.setUpdatedAt(LocalDateTime.now());
        userMap.put(user2.getId(), user2);

        initializeDefaultRules();
    }

    private void initializeDefaultRules() {
        RiskRule rule1 = new RiskRule();
        rule1.setId(ruleIdGenerator.getAndIncrement());
        rule1.setName("单笔金额超过5000元");
        rule1.setRuleType(RuleType.SINGLE_AMOUNT);
        rule1.setPriority(1);
        rule1.setRiskLevel(RiskLevel.HIGH);
        rule1.setActionType(ActionType.FREEZE);
        rule1.setThresholdAmount(new BigDecimal("5000.00"));
        rule1.setEnabled(true);
        rule1.setDescription("单笔交易金额超过5000元，需要人工审核");
        riskRuleMap.put(rule1.getId(), rule1);

        RiskRule rule2 = new RiskRule();
        rule2.setId(ruleIdGenerator.getAndIncrement());
        rule2.setName("5分钟内超过3笔交易");
        rule2.setRuleType(RuleType.SHORT_TERM_FREQUENCY);
        rule2.setPriority(2);
        rule2.setRiskLevel(RiskLevel.MEDIUM);
        rule2.setActionType(ActionType.FREEZE);
        rule2.setThresholdCount(3);
        rule2.setTimeWindowMinutes(5);
        rule2.setEnabled(true);
        rule2.setDescription("5分钟内交易超过3笔，需要人工审核");
        riskRuleMap.put(rule2.getId(), rule2);

        RiskRule rule3 = new RiskRule();
        rule3.setId(ruleIdGenerator.getAndIncrement());
        rule3.setName("当日累计金额超过10000元");
        rule3.setRuleType(RuleType.DAILY_TOTAL_AMOUNT);
        rule3.setPriority(3);
        rule3.setRiskLevel(RiskLevel.MEDIUM);
        rule3.setActionType(ActionType.FREEZE);
        rule3.setThresholdAmount(new BigDecimal("10000.00"));
        rule3.setEnabled(true);
        rule3.setDescription("当日累计交易金额超过10000元，需要人工审核");
        riskRuleMap.put(rule3.getId(), rule3);

        RiskRule rule4 = new RiskRule();
        rule4.setId(ruleIdGenerator.getAndIncrement());
        rule4.setName("连续失败超过2次");
        rule4.setRuleType(RuleType.CONSECUTIVE_FAILURES);
        rule4.setPriority(4);
        rule4.setRiskLevel(RiskLevel.HIGH);
        rule4.setActionType(ActionType.REJECT);
        rule4.setThresholdCount(2);
        rule4.setEnabled(true);
        rule4.setDescription("连续失败超过2次，直接拒绝交易");
        riskRuleMap.put(rule4.getId(), rule4);

        RiskRule rule5 = new RiskRule();
        rule5.setId(ruleIdGenerator.getAndIncrement());
        rule5.setName("黑名单用户");
        rule5.setRuleType(RuleType.BLACKLIST);
        rule5.setPriority(0);
        rule5.setRiskLevel(RiskLevel.HIGH);
        rule5.setActionType(ActionType.BLACKLIST);
        rule5.setEnabled(true);
        rule5.setDescription("黑名单用户直接拒绝交易");
        riskRuleMap.put(rule5.getId(), rule5);
    }

    public Long generateUserId() {
        return userIdGenerator.getAndIncrement();
    }

    public Long generateTransactionId() {
        return transactionIdGenerator.getAndIncrement();
    }

    public Long generateRuleId() {
        return ruleIdGenerator.getAndIncrement();
    }

    public Long generateRuleHitRecordId() {
        return ruleHitRecordIdGenerator.getAndIncrement();
    }

    public Map<Long, User> getUserMap() {
        return userMap;
    }

    public Map<Long, Transaction> getTransactionMap() {
        return transactionMap;
    }

    public Map<Long, RiskRule> getRiskRuleMap() {
        return riskRuleMap;
    }

    public Map<Long, RuleHitRecord> getRuleHitRecordMap() {
        return ruleHitRecordMap;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(userMap.values());
    }

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactionMap.values());
    }

    public List<RiskRule> getAllRules() {
        return new ArrayList<>(riskRuleMap.values());
    }

    public List<RuleHitRecord> getAllRuleHitRecords() {
        return new ArrayList<>(ruleHitRecordMap.values());
    }
}

package com.risk.control.task;

import com.risk.control.entity.Transaction;
import com.risk.control.enums.TransactionStatus;
import com.risk.control.service.TransactionService;
import com.risk.control.service.UserService;
import com.risk.control.store.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TimeoutReviewTask {
    private final DataStore dataStore = DataStore.getInstance();

    @Autowired
    private UserService userService;

    private static final int TIMEOUT_MINUTES = 30;

    @Scheduled(fixedRate = 60000)
    public void scanTimeoutTransactions() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
        List<Transaction> frozenTransactions = dataStore.getAllTransactions().stream()
                .filter(t -> TransactionStatus.FROZEN.equals(t.getStatus()))
                .filter(t -> t.getCreatedAt().isBefore(timeoutThreshold))
                .collect(java.util.stream.Collectors.toList());

        for (Transaction transaction : frozenTransactions) {
            transaction.setStatus(TransactionStatus.TIMEOUT_REJECTED);
            transaction.setReviewComment("超时未审核，自动拒绝");
            transaction.setReviewedAt(LocalDateTime.now());
            transaction.setUpdatedAt(LocalDateTime.now());

            userService.releaseFrozenBalance(transaction.getUserId(), transaction.getAmount());
            userService.incrementConsecutiveFailures(transaction.getUserId());
        }
    }

    public void triggerManualScan() {
        scanTimeoutTransactions();
    }
}

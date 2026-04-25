package com.risk.control.controller;

import com.risk.control.common.Result;
import com.risk.control.entity.RuleHitRecord;
import com.risk.control.entity.Transaction;
import com.risk.control.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public Result<List<Transaction>> getAllTransactions() {
        return Result.success(transactionService.getAllTransactions());
    }

    @GetMapping("/{id}")
    public Result<Transaction> getTransactionById(@PathVariable Long id) {
        Transaction transaction = transactionService.getTransactionById(id);
        if (transaction != null) {
            return Result.success(transaction);
        }
        return Result.error("交易不存在");
    }

    @GetMapping("/pending-review")
    public Result<List<Transaction>> getPendingReviewTransactions() {
        return Result.success(transactionService.getPendingReviewTransactions());
    }

    @PostMapping("/initiate")
    public Result<Transaction> initiateTransaction(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String type = request.get("type").toString();

            Transaction transaction = transactionService.initiateTransaction(userId, amount, type);
            return Result.success(transaction);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/approve")
    public Result<Transaction> approveTransaction(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        try {
            String comment = request != null && request.containsKey("comment") ? request.get("comment") : "人工审核通过";
            Transaction transaction = transactionService.approveTransaction(id, comment);
            return Result.success(transaction);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/reject")
    public Result<Transaction> rejectTransaction(@PathVariable Long id, @RequestBody(required = false) Map<String, String> request) {
        try {
            String comment = request != null && request.containsKey("comment") ? request.get("comment") : "人工审核拒绝";
            Transaction transaction = transactionService.rejectTransaction(id, comment);
            return Result.success(transaction);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/hit-records")
    public Result<List<RuleHitRecord>> getTransactionHitRecords(@PathVariable Long id) {
        return Result.success(transactionService.getTransactionHitRecords(id));
    }
}

package com.risk.control.controller;

import com.risk.control.common.Result;
import com.risk.control.task.TimeoutReviewTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    @Autowired
    private TimeoutReviewTask timeoutReviewTask;

    @PostMapping("/timeout-review/scan")
    public Result<Void> triggerTimeoutReviewScan() {
        try {
            timeoutReviewTask.triggerManualScan();
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

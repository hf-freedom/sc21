package com.risk.control.controller;

import com.risk.control.common.Result;
import com.risk.control.entity.User;
import com.risk.control.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blacklist")
public class BlacklistController {
    @Autowired
    private UserService userService;

    @GetMapping
    public Result<List<User>> getBlacklistedUsers() {
        return Result.success(userService.getBlacklistedUsers());
    }

    @PostMapping("/{userId}")
    public Result<Void> addToBlacklist(@PathVariable Long userId) {
        try {
            userService.addToBlacklist(userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{userId}")
    public Result<Void> removeFromBlacklist(@PathVariable Long userId) {
        try {
            userService.removeFromBlacklist(userId);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

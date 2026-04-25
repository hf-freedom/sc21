package com.risk.control.service;

import com.risk.control.entity.User;
import com.risk.control.enums.RiskLevel;
import com.risk.control.store.DataStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private final DataStore dataStore = DataStore.getInstance();

    public List<User> getAllUsers() {
        return dataStore.getAllUsers();
    }

    public User getUserById(Long id) {
        return dataStore.getUserMap().get(id);
    }

    public User createUser(User user) {
        Long id = dataStore.generateUserId();
        user.setId(id);
        user.setRiskLevel(RiskLevel.LOW);
        user.setBlacklisted(false);
        user.setDailyTransactionCount(0);
        user.setDailyTransactionAmount(BigDecimal.ZERO);
        user.setConsecutiveHighRiskHits(0);
        user.setConsecutiveFailures(0);
        if (user.getBalance() == null) {
            user.setBalance(BigDecimal.ZERO);
        }
        user.setFrozenBalance(BigDecimal.ZERO);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        dataStore.getUserMap().put(id, user);
        return user;
    }

    public User updateUser(User user) {
        User existingUser = dataStore.getUserMap().get(user.getId());
        if (existingUser != null) {
            user.setUpdatedAt(LocalDateTime.now());
            dataStore.getUserMap().put(user.getId(), user);
            return user;
        }
        return null;
    }

    public boolean deleteUser(Long id) {
        return dataStore.getUserMap().remove(id) != null;
    }

    public void updateDailyTransactionStats(Long userId, BigDecimal amount) {
        User user = dataStore.getUserMap().get(userId);
        if (user != null) {
            LocalDate today = LocalDate.now();
            if (!today.equals(user.getLastTransactionDate())) {
                user.setDailyTransactionCount(0);
                user.setDailyTransactionAmount(BigDecimal.ZERO);
                user.setLastTransactionDate(today);
            }
            user.setDailyTransactionCount(user.getDailyTransactionCount() + 1);
            user.setDailyTransactionAmount(user.getDailyTransactionAmount().add(amount));
            user.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void incrementConsecutiveFailures(Long userId) {
        User user = dataStore.getUserMap().get(userId);
        if (user != null) {
            user.setConsecutiveFailures(user.getConsecutiveFailures() + 1);
            user.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void resetConsecutiveFailures(Long userId) {
        User user = dataStore.getUserMap().get(userId);
        if (user != null) {
            user.setConsecutiveFailures(0);
            user.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void incrementConsecutiveHighRiskHits(Long userId) {
        User user = dataStore.getUserMap().get(userId);
        if (user != null) {
            user.setConsecutiveHighRiskHits(user.getConsecutiveHighRiskHits() + 1);
            user.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void resetConsecutiveHighRiskHits(Long userId) {
        User user = dataStore.getUserMap().get(userId);
        if (user != null) {
            user.setConsecutiveHighRiskHits(0);
            user.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void freezeBalance(Long userId, BigDecimal amount) {
        User user = dataStore.getUserMap().get(userId);
        if (user != null && user.getBalance().compareTo(amount) >= 0) {
            user.setBalance(user.getBalance().subtract(amount));
            user.setFrozenBalance(user.getFrozenBalance().add(amount));
            user.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void releaseFrozenBalance(Long userId, BigDecimal amount) {
        User user = dataStore.getUserMap().get(userId);
        if (user != null && user.getFrozenBalance().compareTo(amount) >= 0) {
            user.setFrozenBalance(user.getFrozenBalance().subtract(amount));
            user.setBalance(user.getBalance().add(amount));
            user.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void deductFrozenBalance(Long userId, BigDecimal amount) {
        User user = dataStore.getUserMap().get(userId);
        if (user != null && user.getFrozenBalance().compareTo(amount) >= 0) {
            user.setFrozenBalance(user.getFrozenBalance().subtract(amount));
            user.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void deductBalance(Long userId, BigDecimal amount) {
        User user = dataStore.getUserMap().get(userId);
        if (user != null && user.getBalance().compareTo(amount) >= 0) {
            user.setBalance(user.getBalance().subtract(amount));
            user.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void addToBlacklist(Long userId) {
        User user = dataStore.getUserMap().get(userId);
        if (user != null) {
            user.setBlacklisted(true);
            user.setUpdatedAt(LocalDateTime.now());
        }
    }

    public void removeFromBlacklist(Long userId) {
        User user = dataStore.getUserMap().get(userId);
        if (user != null) {
            user.setBlacklisted(false);
            user.setConsecutiveHighRiskHits(0);
            user.setUpdatedAt(LocalDateTime.now());
        }
    }

    public List<User> getBlacklistedUsers() {
        return dataStore.getAllUsers().stream()
                .filter(User::isBlacklisted)
                .collect(java.util.stream.Collectors.toList());
    }
}

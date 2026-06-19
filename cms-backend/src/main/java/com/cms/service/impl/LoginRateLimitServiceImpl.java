package com.cms.service.impl;

import com.cms.common.exception.BusinessException;
import com.cms.service.LoginRateLimitService;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimitServiceImpl implements LoginRateLimitService {

    private static final int MAX_FAILURES = 5;
    private static final long LOCK_DURATION_MS = 15 * 60 * 1000L;

    private final ConcurrentHashMap<String, FailureInfo> failureMap = new ConcurrentHashMap<>();

    @Override
    public void checkRateLimit(String username) {
        FailureInfo info = failureMap.get(username);
        if (info != null && info.failCount >= MAX_FAILURES) {
            long elapsed = System.currentTimeMillis() - info.firstFailTime;
            if (elapsed < LOCK_DURATION_MS) {
                int remainMinutes = (int) Math.ceil((LOCK_DURATION_MS - elapsed) / 60000.0);
                throw new BusinessException("登录失败次数过多，请" + remainMinutes + "分钟后再试");
            }
            // 锁定时间已过，清除记录
            failureMap.remove(username);
        }
    }

    @Override
    public void recordFailure(String username) {
        failureMap.compute(username, (key, existing) -> {
            if (existing == null) {
                return new FailureInfo(1, System.currentTimeMillis());
            }
            existing.failCount++;
            return existing;
        });
    }

    @Override
    public void clearFailure(String username) {
        failureMap.remove(username);
    }

    private static class FailureInfo {
        int failCount;
        long firstFailTime;

        FailureInfo(int failCount, long firstFailTime) {
            this.failCount = failCount;
            this.firstFailTime = firstFailTime;
        }
    }
}

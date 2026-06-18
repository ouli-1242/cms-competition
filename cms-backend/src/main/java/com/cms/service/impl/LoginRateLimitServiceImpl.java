package com.cms.service.impl;

import com.cms.service.LoginRateLimitService;
import org.springframework.stereotype.Service;

/**
 * 登录限流服务（无 Redis 时为空实现，不做任何限流）
 * 如需限流功能，请安装 Redis 并恢复 Redis 相关实现
 */
@Service
public class LoginRateLimitServiceImpl implements LoginRateLimitService {

    @Override
    public void checkRateLimit(String username) {
        // 无 Redis，不做限流检查
    }

    @Override
    public void recordFailure(String username) {
        // 无 Redis，不记录失败次数
    }

    @Override
    public void clearFailure(String username) {
        // 无 Redis，不清除失败计数
    }
}

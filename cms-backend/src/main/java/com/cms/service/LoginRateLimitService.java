package com.cms.service;

public interface LoginRateLimitService {
    void checkRateLimit(String username);
    void recordFailure(String username);
    void clearFailure(String username);
}

package com.cms.service;

public interface NotificationService {
    /**
     * 异步发送通知
     */
    void asyncNotify(Long userId, String content, String type);
}

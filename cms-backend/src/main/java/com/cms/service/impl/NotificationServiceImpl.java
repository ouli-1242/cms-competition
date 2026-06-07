package com.cms.service.impl;

import com.cms.entity.Message;
import com.cms.mapper.MessageMapper;
import com.cms.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    @Async
    public void asyncNotify(Long userId, String content, String type) {
        try {
            Message m = new Message();
            m.setUserId(userId);
            m.setType(type);
            m.setTitle(getTitle(type));
            m.setContent(content);
            m.setIsRead(0);
            messageMapper.insert(m);
            log.info("异步通知: userId={}, type={}", userId, type);
        } catch (Exception e) {
            log.error("通知失败", e);
        }
    }

    private String getTitle(String type) {
        switch (type) {
            case "REGISTRATION": return "报名通知";
            case "TEAM":         return "团队通知";
            case "SYSTEM":       return "系统通知";
            default:             return "通知";
        }
    }
}

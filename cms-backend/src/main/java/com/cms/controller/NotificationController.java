package com.cms.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.entity.Message;
import com.cms.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通知消息（所有已登录用户可用）
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired private MessageMapper messageMapper;

    /** 消息列表（分页） */
    @GetMapping("/list")
    public Result<Page<Message>> list(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "20") Integer pageSize,
        @RequestParam(required = false) Integer isRead) {
        Long userId = SecurityUtil.currentUserId();
        Page<Message> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Message::getUserId, userId);
        if (isRead != null) wrapper.eq(Message::getIsRead, isRead);
        wrapper.orderByDesc(Message::getCreateTime);
        return Result.success(messageMapper.selectPage(page, wrapper));
    }

    /** 未读消息数 */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        Long userId = SecurityUtil.currentUserId();
        Long count = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
            .eq(Message::getUserId, userId)
            .eq(Message::getIsRead, 0));
        return Result.success(count);
    }

    /** 标记已读 */
    @PutMapping("/{id}/read")
    public Result markRead(@PathVariable Long id) {
        Long userId = SecurityUtil.currentUserId();
        Message msg = messageMapper.selectById(id);
        if (msg == null || !msg.getUserId().equals(userId)) return Result.error("消息不存在");
        msg.setIsRead(1);
        messageMapper.updateById(msg);
        return Result.success();
    }

    /** 全部已读 */
    @PutMapping("/read-all")
    public Result markAllRead() {
        Long userId = SecurityUtil.currentUserId();
        messageMapper.update(null, new UpdateWrapper<Message>()
            .eq("user_id", userId)
            .eq("is_read", 0)
            .set("is_read", 1));
        return Result.success();
    }
}

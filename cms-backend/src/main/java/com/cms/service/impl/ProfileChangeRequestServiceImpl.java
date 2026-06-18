package com.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cms.common.exception.BusinessException;
import com.cms.entity.ProfileChangeRequest;
import com.cms.entity.User;
import com.cms.mapper.ProfileChangeRequestMapper;
import com.cms.mapper.UserMapper;
import com.cms.service.ProfileChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProfileChangeRequestServiceImpl
        extends ServiceImpl<ProfileChangeRequestMapper, ProfileChangeRequest>
        implements ProfileChangeRequestService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ProfileChangeRequest submitRequest(Long userId, String fieldName, String newValue) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        String oldValue;
        if ("username".equals(fieldName)) {
            oldValue = user.getUsername();
            // 检查新学号是否已被占用
            long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, newValue)
                    .ne(User::getId, userId)
            );
            if (count > 0) throw new BusinessException("该学号已被其他用户使用");
        } else if ("email".equals(fieldName)) {
            oldValue = user.getEmail();
        } else {
            throw new BusinessException("不支持修改该字段");
        }

        // 值未变化直接返回 null
        if (newValue.equals(oldValue)) return null;

        // 关闭同一用户同一字段的旧 pending 请求
        ProfileChangeRequest reject = new ProfileChangeRequest();
        reject.setStatus(2);
        this.update(reject, new LambdaQueryWrapper<ProfileChangeRequest>()
                .eq(ProfileChangeRequest::getUserId, userId)
                .eq(ProfileChangeRequest::getFieldName, fieldName)
                .eq(ProfileChangeRequest::getStatus, 0));

        ProfileChangeRequest req = new ProfileChangeRequest();
        req.setUserId(userId);
        req.setFieldName(fieldName);
        req.setOldValue(oldValue);
        req.setNewValue(newValue);
        req.setStatus(0);
        this.save(req);
        return req;
    }

    @Override
    public List<ProfileChangeRequest> getMyRequests(Long userId) {
        return this.list(new LambdaQueryWrapper<ProfileChangeRequest>()
                .eq(ProfileChangeRequest::getUserId, userId)
                .orderByDesc(ProfileChangeRequest::getCreateTime));
    }

    @Override
    public void reviewRequest(Long requestId, boolean approved, String remark, Long reviewerId) {
        ProfileChangeRequest req = this.getById(requestId);
        if (req == null) throw new BusinessException("申请不存在");
        if (req.getStatus() != 0) throw new BusinessException("该申请已处理");

        req.setReviewTime(LocalDateTime.now());
        req.setReviewerId(reviewerId);
        req.setReviewRemark(remark);

        if (approved) {
            req.setStatus(1);
            // 实际更新用户字段
            User user = new User();
            user.setId(req.getUserId());
            if ("username".equals(req.getFieldName())) {
                user.setUsername(req.getNewValue());
            } else if ("email".equals(req.getFieldName())) {
                user.setEmail(req.getNewValue());
            }
            userMapper.updateById(user);
        } else {
            req.setStatus(2);
        }
        this.updateById(req);
    }
}

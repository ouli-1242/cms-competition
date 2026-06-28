package com.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cms.common.exception.BusinessException;
import com.cms.entity.Competition;
import com.cms.entity.Registration;
import com.cms.entity.User;
import com.cms.mapper.CompetitionMapper;
import com.cms.mapper.RegistrationMapper;
import com.cms.mapper.UserMapper;
import com.cms.service.NotificationService;
import com.cms.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService {

    @Autowired
    private CompetitionMapper competitionMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public Registration registerIndividual(Long competitionId, Long userId, String description, String attachment) {
        Competition c = competitionMapper.selectById(competitionId);
        if (c == null) throw new BusinessException("竞赛不存在");
        if (c.getStatus() != 1) throw new BusinessException("竞赛未上架");
        if (c.getType() != 1) throw new BusinessException("该竞赛为团队赛，请组队报名");

        LocalDateTime now = LocalDateTime.now();
        if (c.getRegisterStart() != null && now.isBefore(c.getRegisterStart())) {
            throw new BusinessException("报名未开始");
        }
        if (c.getRegisterEnd() != null && now.isAfter(c.getRegisterEnd())) {
            throw new BusinessException("报名已截止");
        }

        Long exists = this.count(new LambdaQueryWrapper<Registration>()
            .eq(Registration::getCompetitionId, competitionId)
            .eq(Registration::getUserId, userId));
        if (exists > 0) throw new BusinessException("您已报名该竞赛");

        Registration r = new Registration();
        r.setCompetitionId(competitionId);
        r.setUserId(userId);
        r.setStatus(0);
        r.setDescription(description);
        r.setAttachment(attachment);
        r.setRegisterTime(LocalDateTime.now());
        this.save(r);

        // 异步通知管理员
        User user = userMapper.selectById(userId);
        String userName = (user != null && user.getRealName() != null) ? user.getRealName() : String.valueOf(userId);
        notificationService.asyncNotify(c.getPublisherId(), "新报名：" + userName + " 报名了 " + c.getTitle(), "REGISTRATION");
        return r;
    }

    @Override
    public void cancelRegistration(Long id, Long userId) {
        Registration r = this.getById(id);
        if (r == null) throw new BusinessException("记录不存在");
        if (!r.getUserId().equals(userId)) throw new BusinessException("只能取消自己的报名");
        if (r.getStatus() != 0) throw new BusinessException("已通过/拒绝的报名不能取消");
        this.removeById(id);
    }

    @Override
    @Transactional
    public void review(Long id, Boolean pass, String remark, Long reviewerId) {
        Registration r = this.getById(id);
        if (r == null) throw new BusinessException("记录不存在");
        r.setStatus(pass ? 1 : 2);
        r.setReviewerId(reviewerId);
        r.setReviewRemark(remark);
        r.setReviewTime(LocalDateTime.now());
        this.updateById(r);

        notificationService.asyncNotify(r.getUserId(),
            pass ? "您的报名已通过" : "您的报名被拒绝：" + remark, "REGISTRATION");
    }

    @Override
    public List<Registration> listForExport(Long competitionId, Integer status, String studentName, Long teacherId) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        if (competitionId != null) wrapper.eq(Registration::getCompetitionId, competitionId);
        if (status != null) wrapper.eq(Registration::getStatus, status);
        if (studentName != null && !studentName.isEmpty()) {
            LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
            userQuery.like(User::getRealName, studentName).select(User::getId);
            List<Long> userIds = userMapper.selectList(userQuery).stream().map(User::getId).collect(Collectors.toList());
            if (userIds.isEmpty()) userIds.add(-1L); // no match
            wrapper.in(Registration::getUserId, userIds);
        }
        wrapper.orderByDesc(Registration::getRegisterTime);
        return this.list(wrapper);
    }

}

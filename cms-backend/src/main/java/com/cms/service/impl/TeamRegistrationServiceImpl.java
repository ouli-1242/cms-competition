package com.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cms.common.exception.BusinessException;
import com.cms.entity.Team;
import com.cms.entity.TeamRegistration;
import com.cms.mapper.TeamMapper;
import com.cms.mapper.TeamRegistrationMapper;
import com.cms.service.NotificationService;
import com.cms.service.TeamRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TeamRegistrationServiceImpl extends ServiceImpl<TeamRegistrationMapper, TeamRegistration> implements TeamRegistrationService {

    @Autowired private TeamMapper teamMapper;
    @Autowired private NotificationService notificationService;

    @Override
    @Transactional
    public void review(Long id, Boolean pass, String remark, Long reviewerId) {
        TeamRegistration reg = this.getById(id);
        if (reg == null) throw new BusinessException("记录不存在");
        reg.setStatus(pass ? 1 : 2);
        reg.setReviewerId(reviewerId);
        reg.setReviewRemark(remark);
        reg.setReviewTime(LocalDateTime.now());
        this.updateById(reg);

        Team team = teamMapper.selectById(reg.getTeamId());
        if (team != null) {
            notificationService.asyncNotify(team.getCaptainId(),
                pass ? "团队报名已通过" : "团队报名被拒绝：" + remark, "REGISTRATION");
        }
    }
}

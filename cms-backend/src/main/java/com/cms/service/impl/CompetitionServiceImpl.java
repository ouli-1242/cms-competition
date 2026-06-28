package com.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cms.common.exception.BusinessException;
import com.cms.dto.CompetitionDTO;
import com.cms.entity.Competition;
import com.cms.entity.Registration;
import com.cms.entity.TeamRegistration;
import com.cms.mapper.CompetitionMapper;
import com.cms.mapper.RegistrationMapper;
import com.cms.mapper.TeamRegistrationMapper;
import com.cms.service.CompetitionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompetitionServiceImpl extends ServiceImpl<CompetitionMapper, Competition> implements CompetitionService {

    @Autowired
    private RegistrationMapper registrationMapper;
    @Autowired
    private TeamRegistrationMapper teamRegistrationMapper;

    @Override
    public Competition publish(CompetitionDTO dto, Long publisherId) {
        Competition c = new Competition();
        BeanUtils.copyProperties(dto, c);
        c.setPublisherId(publisherId);
        c.setStatus(0);  // 默认下架
        this.save(c);
        return c;
    }

    @Override
    public void updateCompetition(Long id, CompetitionDTO dto) {
        Competition old = this.getById(id);
        if (old == null) throw new BusinessException("竞赛不存在");

        long regCount = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getCompetitionId, id))
            + teamRegistrationMapper.selectCount(new LambdaQueryWrapper<TeamRegistration>()
                .eq(TeamRegistration::getCompetitionId, id));

        if (regCount > 0) {
            // 已有报名记录，类型/人数/时间不可改
            if (dto.getType() != null && !dto.getType().equals(old.getType())) {
                throw new BusinessException("已有报名记录，竞赛类型不可修改");
            }
            if (dto.getMaxTeamSize() != null && !dto.getMaxTeamSize().equals(old.getMaxTeamSize())) {
                throw new BusinessException("已有报名记录，团队人数不可修改");
            }
            if (dto.getRegisterStart() != null && !dto.getRegisterStart().equals(old.getRegisterStart())) {
                throw new BusinessException("已有报名记录，报名时间不可修改");
            }
        }
        Competition c = new Competition();
        BeanUtils.copyProperties(dto, c);
        c.setId(id);
        this.updateById(c);
    }


    @Override
    public void changeStatus(Long id, Integer status) {
        Competition c = this.getById(id);
        if (c == null) throw new BusinessException("竞赛不存在");
        c.setStatus(status);
        this.updateById(c);
    }


    @Override
    public void deleteCompetition(Long id) {
        Competition c = this.getById(id);
        if (c == null) throw new BusinessException("竞赛不存在");
        long count = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getCompetitionId, id))
            + teamRegistrationMapper.selectCount(new LambdaQueryWrapper<TeamRegistration>()
                .eq(TeamRegistration::getCompetitionId, id));
        if (count > 0) {
            throw new BusinessException("已有报名记录，不可删除");
        }
        this.removeById(id);
    }


    @Override
    public List<Long> listIdsByTeacher(Long teacherId) {
        return baseMapper.selectIdsByTeacher(teacherId);
    }

}

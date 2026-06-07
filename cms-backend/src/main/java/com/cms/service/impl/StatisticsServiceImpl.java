package com.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cms.entity.Registration;
import com.cms.entity.TeamRegistration;
import com.cms.mapper.RegistrationMapper;
import com.cms.mapper.TeamRegistrationMapper;
import com.cms.service.StatisticsService;
import com.cms.vo.StatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired private RegistrationMapper registrationMapper;
    @Autowired private TeamRegistrationMapper teamRegistrationMapper;

    @Override
    public StatisticsVO overview(Long teacherId) {
        StatisticsVO vo = new StatisticsVO();
        LambdaQueryWrapper<Registration> w1 = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<TeamRegistration> w2 = new LambdaQueryWrapper<>();
        if (teacherId != null) {
            w1.inSql(Registration::getCompetitionId,
                "SELECT id FROM competition WHERE teacher_id = " + teacherId);
            w2.inSql(TeamRegistration::getCompetitionId,
                "SELECT id FROM competition WHERE teacher_id = " + teacherId);
        }
        int indivTotal = registrationMapper.selectCount(w1);
        int teamTotal = teamRegistrationMapper.selectCount(w2);
        vo.setIndividualCount(indivTotal);
        vo.setTeamCount(teamTotal);
        vo.setTotalCount(indivTotal + teamTotal);

        LambdaQueryWrapper<Registration> w1p = w1.clone().eq(Registration::getStatus, 1);
        LambdaQueryWrapper<TeamRegistration> w2p = w2.clone().eq(TeamRegistration::getStatus, 1);
        int passed = registrationMapper.selectCount(w1p) + teamRegistrationMapper.selectCount(w2p);
        vo.setPassedCount(passed);
        vo.setPassRate(vo.getTotalCount() == 0 ? 0.0 : passed * 100.0 / vo.getTotalCount());
        return vo;
    }

    @Override
    public StatisticsVO competitionStat(Long competitionId) {
        StatisticsVO vo = new StatisticsVO();
        vo.setCompetitionCount(1L);
        int indiv = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
            .eq(Registration::getCompetitionId, competitionId));
        int team = teamRegistrationMapper.selectCount(new LambdaQueryWrapper<TeamRegistration>()
            .eq(TeamRegistration::getCompetitionId, competitionId));
        vo.setIndividualCount(indiv);
        vo.setTeamCount(team);
        vo.setTotalCount(indiv + team);
        int passed = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getCompetitionId, competitionId).eq(Registration::getStatus, 1))
            + teamRegistrationMapper.selectCount(new LambdaQueryWrapper<TeamRegistration>()
                .eq(TeamRegistration::getCompetitionId, competitionId).eq(TeamRegistration::getStatus, 1));
        vo.setPassedCount(passed);
        vo.setPassRate(vo.getTotalCount() == 0 ? 0.0 : passed * 100.0 / vo.getTotalCount());
        return vo;
    }
}

package com.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cms.entity.Competition;
import com.cms.entity.Registration;
import com.cms.entity.Team;
import com.cms.entity.TeamRegistration;
import com.cms.mapper.CompetitionMapper;
import com.cms.mapper.RegistrationMapper;
import com.cms.mapper.TeamMapper;
import com.cms.mapper.TeamRegistrationMapper;
import com.cms.service.StatisticsService;
import com.cms.vo.StatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired private RegistrationMapper registrationMapper;
    @Autowired private TeamRegistrationMapper teamRegistrationMapper;
    @Autowired private CompetitionMapper competitionMapper;
    @Autowired private TeamMapper teamMapper;

    /**
     * 获取已解散团队ID列表，用于排除已解散团队的报名统计
     */
    private List<Long> getDissolvedTeamIds() {
        LambdaQueryWrapper<Team> w = new LambdaQueryWrapper<>();
        w.isNotNull(Team::getDissolvedAt).select(Team::getId);
        List<Long> ids = teamMapper.selectList(w).stream().map(Team::getId).collect(Collectors.toList());
        return ids.isEmpty() ? null : ids;
    }

    @Override
    public StatisticsVO overview(Long teacherId) {
        StatisticsVO vo = new StatisticsVO();
        LambdaQueryWrapper<Registration> w1 = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<TeamRegistration> w2 = new LambdaQueryWrapper<>();
        List<Long> compIds = null;
        if (teacherId != null) {
            LambdaQueryWrapper<Competition> compQuery = new LambdaQueryWrapper<>();
            compQuery.eq(Competition::getTeacherId, teacherId).select(Competition::getId);
            compIds = competitionMapper.selectList(compQuery).stream().map(Competition::getId).collect(Collectors.toList());
            if (compIds.isEmpty()) compIds.add(-1L); // no match
            w1.in(Registration::getCompetitionId, compIds);
            w2.in(TeamRegistration::getCompetitionId, compIds);
        }
        // 排除已解散团队
        List<Long> dissolvedIds = getDissolvedTeamIds();
        if (dissolvedIds != null) {
            w2.notIn(TeamRegistration::getTeamId, dissolvedIds);
        }
        long indivTotal = registrationMapper.selectCount(w1);
        long teamTotal = teamRegistrationMapper.selectCount(w2);
        vo.setIndividualCount((int) (long) indivTotal);
        vo.setTeamCount((int) (long) teamTotal);
        vo.setTotalCount((int) (indivTotal + teamTotal));

        // 设置竞赛数量
        if (compIds != null) {
            vo.setCompetitionCount((long) compIds.size());
        } else {
            vo.setCompetitionCount(competitionMapper.selectCount(new LambdaQueryWrapper<>()));
        }

        LambdaQueryWrapper<Registration> w1p = w1.clone().eq(Registration::getStatus, 1);
        LambdaQueryWrapper<TeamRegistration> w2p = w2.clone().eq(TeamRegistration::getStatus, 1);
        long passed = registrationMapper.selectCount(w1p) + teamRegistrationMapper.selectCount(w2p);
        vo.setPassedCount((int) passed);

        long pending = registrationMapper.selectCount(w1.clone().eq(Registration::getStatus, 0))
            + teamRegistrationMapper.selectCount(w2.clone().eq(TeamRegistration::getStatus, 0));
        vo.setPendingCount((int) pending);

        long rejected = registrationMapper.selectCount(w1.clone().eq(Registration::getStatus, 2))
            + teamRegistrationMapper.selectCount(w2.clone().eq(TeamRegistration::getStatus, 2));
        vo.setRejectedCount((int) rejected);

        vo.setPassRate(vo.getTotalCount() == 0 ? 0.0 : passed * 100.0 / vo.getTotalCount());
        return vo;
    }

    @Override
    public StatisticsVO competitionStat(Long competitionId) {
        StatisticsVO vo = new StatisticsVO();
        vo.setCompetitionCount(1L);

        List<Long> dissolvedIds = getDissolvedTeamIds();

        LambdaQueryWrapper<TeamRegistration> baseTeamQuery = new LambdaQueryWrapper<TeamRegistration>()
            .eq(TeamRegistration::getCompetitionId, competitionId);
        if (dissolvedIds != null) {
            baseTeamQuery.notIn(TeamRegistration::getTeamId, dissolvedIds);
        }

        long indiv = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
            .eq(Registration::getCompetitionId, competitionId));
        long team = teamRegistrationMapper.selectCount(baseTeamQuery);
        vo.setIndividualCount((int) indiv);
        vo.setTeamCount((int) team);
        vo.setTotalCount((int) (indiv + team));
        long passed = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getCompetitionId, competitionId).eq(Registration::getStatus, 1))
            + teamRegistrationMapper.selectCount(baseTeamQuery.clone().eq(TeamRegistration::getStatus, 1));
        vo.setPassedCount((int) passed);

        long pending = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getCompetitionId, competitionId).eq(Registration::getStatus, 0))
            + teamRegistrationMapper.selectCount(baseTeamQuery.clone().eq(TeamRegistration::getStatus, 0));
        vo.setPendingCount((int) pending);

        long rejected = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getCompetitionId, competitionId).eq(Registration::getStatus, 2))
            + teamRegistrationMapper.selectCount(baseTeamQuery.clone().eq(TeamRegistration::getStatus, 2));
        vo.setRejectedCount((int) rejected);

        vo.setPassRate(vo.getTotalCount() == 0 ? 0.0 : passed * 100.0 / vo.getTotalCount());
        return vo;
    }
}

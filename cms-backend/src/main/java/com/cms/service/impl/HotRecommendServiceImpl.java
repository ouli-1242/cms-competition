package com.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cms.entity.Competition;
import com.cms.entity.HotRecommend;
import com.cms.entity.Registration;
import com.cms.entity.TeamRegistration;
import com.cms.mapper.CompetitionMapper;
import com.cms.mapper.HotRecommendMapper;
import com.cms.mapper.RegistrationMapper;
import com.cms.mapper.TeamRegistrationMapper;
import com.cms.service.HotRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HotRecommendServiceImpl implements HotRecommendService {

    @Autowired private HotRecommendMapper hotMapper;
    @Autowired private CompetitionMapper competitionMapper;
    @Autowired private RegistrationMapper registrationMapper;
    @Autowired private TeamRegistrationMapper teamRegistrationMapper;

    @Override
    public void add(Long competitionId, Integer sort) {
        HotRecommend h = hotMapper.selectOne(new LambdaQueryWrapper<HotRecommend>()
            .eq(HotRecommend::getCompetitionId, competitionId));
        if (h == null) {
            h = new HotRecommend();
            h.setCompetitionId(competitionId);
            h.setSort(sort);
            hotMapper.insert(h);
        } else {
            h.setSort(sort);
            hotMapper.updateById(h);
        }
    }

    @Override
    public void cancel(Long competitionId) {
        hotMapper.delete(new LambdaQueryWrapper<HotRecommend>()
            .eq(HotRecommend::getCompetitionId, competitionId));
    }

    @Override
    @Transactional
    public void autoRecommend(Integer topN) {
        hotMapper.delete(new LambdaQueryWrapper<>());
        List<Competition> comps = competitionMapper.selectList(new LambdaQueryWrapper<Competition>()
            .eq(Competition::getStatus, 1));
        comps.sort((a, b) -> {
            long ca = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                    .eq(Registration::getCompetitionId, a.getId()))
                + teamRegistrationMapper.selectCount(new LambdaQueryWrapper<TeamRegistration>()
                    .eq(TeamRegistration::getCompetitionId, a.getId()));
            long cb = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                    .eq(Registration::getCompetitionId, b.getId()))
                + teamRegistrationMapper.selectCount(new LambdaQueryWrapper<TeamRegistration>()
                    .eq(TeamRegistration::getCompetitionId, b.getId()));
            return Long.compare(cb, ca);
        });
        for (int i = 0; i < Math.min(topN, comps.size()); i++) {
            HotRecommend h = new HotRecommend();
            h.setCompetitionId(comps.get(i).getId());
            h.setSort(topN - i);
            hotMapper.insert(h);
        }
    }

    @Override
    public List<Competition> listRecommend() {
        List<HotRecommend> hots = hotMapper.selectList(new LambdaQueryWrapper<HotRecommend>()
            .orderByDesc(HotRecommend::getSort));
        List<Long> compIds = hots.stream().map(HotRecommend::getCompetitionId).collect(Collectors.toList());
        if (compIds.isEmpty()) return Collections.emptyList();

        List<Competition> comps = competitionMapper.selectList(new LambdaQueryWrapper<Competition>()
            .in(Competition::getId, compIds)
            .eq(Competition::getStatus, 1));
        Map<Long, Competition> map = comps.stream().collect(Collectors.toMap(Competition::getId, c -> c));
        return compIds.stream().map(map::get).filter(Objects::nonNull).collect(Collectors.toList());
    }
}

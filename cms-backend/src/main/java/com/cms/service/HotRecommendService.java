package com.cms.service;

import com.cms.entity.Competition;

import java.util.List;

public interface HotRecommendService {
    void add(Long competitionId, Integer sort);
    void cancel(Long competitionId);
    void autoRecommend(Integer topN);
    List<Competition> listRecommend();
}

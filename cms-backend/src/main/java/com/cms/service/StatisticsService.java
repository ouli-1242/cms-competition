package com.cms.service;

import com.cms.vo.StatisticsVO;
import java.util.List;

public interface StatisticsService {
    StatisticsVO overview(Long teacherId);
    StatisticsVO competitionStat(Long competitionId);
}

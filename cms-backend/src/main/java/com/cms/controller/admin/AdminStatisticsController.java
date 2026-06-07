package com.cms.controller.admin;

import com.cms.common.result.Result;
import com.cms.service.StatisticsService;
import com.cms.vo.StatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/statistics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminStatisticsController {

    @Autowired private StatisticsService statisticsService;

    @GetMapping("/overview")
    public Result<StatisticsVO> overview() {
        return Result.success(statisticsService.overview(null));
    }

    @GetMapping("/competition/{id}")
    public Result<StatisticsVO> competitionStat(@PathVariable Long id) {
        return Result.success(statisticsService.competitionStat(id));
    }
}

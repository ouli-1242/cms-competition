package com.cms.controller.teacher;

import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.service.StatisticsService;
import com.cms.vo.StatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher/statistics")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherStatisticsController {

    @Autowired private StatisticsService statisticsService;

    /** 我的统计 */
    @GetMapping
    public Result<StatisticsVO> myStatistics() {
        return Result.success(statisticsService.overview(SecurityUtil.currentUserId()));
    }

    /** 单个竞赛统计 */
    @GetMapping("/competition/{id}")
    public Result<StatisticsVO> competitionStat(@PathVariable Long id) {
        return Result.success(statisticsService.competitionStat(id));
    }
}

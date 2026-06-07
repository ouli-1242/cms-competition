package com.cms.controller.admin;

import com.cms.common.result.Result;
import com.cms.service.HotRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/hot")
@PreAuthorize("hasRole('ADMIN')")
public class AdminHotController {

    @Autowired private HotRecommendService hotRecommendService;

    @PostMapping
    public Result add(@RequestParam Long competitionId, @RequestParam Integer sort) {
        hotRecommendService.add(competitionId, sort);
        return Result.success();
    }

    @DeleteMapping("/{competitionId}")
    public Result cancel(@PathVariable Long competitionId) {
        hotRecommendService.cancel(competitionId);
        return Result.success();
    }

    @PostMapping("/auto")
    public Result autoRecommend(@RequestParam(defaultValue = "10") Integer topN) {
        hotRecommendService.autoRecommend(topN);
        return Result.success();
    }
}

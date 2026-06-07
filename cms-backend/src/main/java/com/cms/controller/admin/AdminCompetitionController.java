package com.cms.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.dto.CompetitionDTO;
import com.cms.entity.Competition;
import com.cms.service.CompetitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/competition")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCompetitionController {

    @Autowired private CompetitionService competitionService;
    @Autowired private com.baomidou.mybatisplus.extension.service.IService<Competition> compService;

    @GetMapping("/page")
    public Result<Page<Competition>> page(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer status) {
        Page<Competition> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Competition::getTitle, keyword);
        if (status  != null) wrapper.eq(Competition::getStatus, status);
        wrapper.orderByDesc(Competition::getCreateTime);
        return Result.success(compService.page(page, wrapper));
    }

    @PostMapping
    public Result<Competition> publish(@RequestBody CompetitionDTO dto) {
        return Result.success(competitionService.publish(dto, SecurityUtil.currentUserId()));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody CompetitionDTO dto) {
        competitionService.updateCompetition(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        competitionService.changeStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        competitionService.deleteCompetition(id);
        return Result.success();
    }
}

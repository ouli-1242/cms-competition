package com.cms.controller.teacher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.entity.Competition;
import com.cms.service.CompetitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/teacher/competition")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherCompetitionController {

    @Autowired
    private CompetitionService competitionService;
    @Autowired
    private com.baomidou.mybatisplus.extension.service.IService<Competition> compService;

    /** 我指导的竞赛 */
    @GetMapping("/page")
    public Result<Page<Competition>> page(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer status) {
        Page<Competition> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getTeacherId, SecurityUtil.currentUserId());
        if (keyword != null && !keyword.isEmpty()) wrapper.like(Competition::getTitle, keyword);
        if (status  != null) wrapper.eq(Competition::getStatus, status);
        wrapper.orderByDesc(Competition::getCreateTime);
        return Result.success(compService.page(page, wrapper));
    }

    /** 竞赛详情 */
    @GetMapping("/{id}")
    public Result<Competition> detail(@PathVariable Long id) {
        Competition c = compService.getById(id);
        if (c == null) return Result.error("竞赛不存在");
        if (!Objects.equals(c.getTeacherId(), SecurityUtil.currentUserId())) {
            return Result.error(403, "您不是该竞赛的指导老师");
        }
        return Result.success(c);
    }
}

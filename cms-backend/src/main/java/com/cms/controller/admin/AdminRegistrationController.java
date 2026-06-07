package com.cms.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.entity.Registration;
import com.cms.entity.TeamRegistration;
import com.cms.mapper.RegistrationMapper;
import com.cms.mapper.TeamRegistrationMapper;
import com.cms.service.RegistrationService;
import com.cms.service.TeamRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRegistrationController {

    @Autowired private RegistrationService registrationService;
    @Autowired private TeamRegistrationService teamRegistrationService;
    @Autowired private RegistrationMapper registrationMapper;
    @Autowired private TeamRegistrationMapper teamRegistrationMapper;

    @GetMapping("/registration/page")
    public Result<Page<Registration>> registrationPage(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Long competitionId,
        @RequestParam(required = false) Integer status,
        @RequestParam(required = false) String studentName) {
        Page<Registration> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        if (competitionId != null) wrapper.eq(Registration::getCompetitionId, competitionId);
        if (status != null) wrapper.eq(Registration::getStatus, status);
        if (studentName != null && !studentName.isEmpty()) {
            wrapper.inSql(Registration::getUserId,
                "SELECT id FROM user WHERE real_name LIKE '%" + studentName + "%'");
        }
        wrapper.orderByDesc(Registration::getRegisterTime);
        return Result.success(registrationMapper.selectPage(page, wrapper));
    }

    @PutMapping("/registration/{id}/review")
    public Result reviewRegistration(@PathVariable Long id,
                                      @RequestParam Boolean pass,
                                      @RequestParam(required = false) String remark) {
        registrationService.review(id, pass, remark, SecurityUtil.currentUserId());
        return Result.success();
    }

    @GetMapping("/team-registration/page")
    public Result<Page<TeamRegistration>> teamRegPage(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Long competitionId,
        @RequestParam(required = false) Integer status) {
        Page<TeamRegistration> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TeamRegistration> wrapper = new LambdaQueryWrapper<>();
        if (competitionId != null) wrapper.eq(TeamRegistration::getCompetitionId, competitionId);
        if (status != null) wrapper.eq(TeamRegistration::getStatus, status);
        wrapper.orderByDesc(TeamRegistration::getRegisterTime);
        return Result.success(teamRegistrationMapper.selectPage(page, wrapper));
    }

    @PutMapping("/team-registration/{id}/review")
    public Result reviewTeamReg(@PathVariable Long id,
                                 @RequestParam Boolean pass,
                                 @RequestParam(required = false) String remark) {
        teamRegistrationService.review(id, pass, remark, SecurityUtil.currentUserId());
        return Result.success();
    }
}

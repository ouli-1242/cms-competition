package com.cms.controller.student;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.entity.Registration;
import com.cms.entity.TeamRegistration;
import com.cms.mapper.RegistrationMapper;
import com.cms.mapper.TeamRegistrationMapper;
import com.cms.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/registration")
@PreAuthorize("hasRole('STUDENT')")
public class StudentRegistrationController {

    @Autowired private RegistrationService registrationService;
    @Autowired private RegistrationMapper registrationMapper;
    @Autowired private TeamRegistrationMapper teamRegistrationMapper;

    /** 个人赛报名 */
    @PostMapping
    public Result<Registration> register(@RequestParam Long competitionId,
                                          @RequestParam(required = false) String description,
                                          @RequestParam(required = false) String attachment) {
        return Result.success(registrationService.registerIndividual(
            competitionId, SecurityUtil.currentUserId(), description, attachment));
    }

    /** 我的报名记录 */
    @GetMapping("/page")
    public Result<Page<Registration>> myPage(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Integer status) {
        Page<Registration> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getUserId, SecurityUtil.currentUserId());
        if (status != null) wrapper.eq(Registration::getStatus, status);
        wrapper.orderByDesc(Registration::getRegisterTime);
        return Result.success(registrationMapper.selectPage(page, wrapper));
    }

    /** 我的团队报名 */
    @GetMapping("/team/page")
    public Result<Page<TeamRegistration>> myTeamPage(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<TeamRegistration> page = new Page<>(pageNum, pageSize);
        // 简化：返回当前用户所在团队的所有报名（管理员/老师再调管理员接口）
        return Result.success(teamRegistrationMapper.selectPage(page, new LambdaQueryWrapper<>()));
    }
}

package com.cms.controller.student;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.entity.Registration;
import com.cms.entity.Team;
import com.cms.entity.TeamMember;
import com.cms.entity.TeamRegistration;
import com.cms.mapper.RegistrationMapper;
import com.cms.mapper.TeamMapper;
import com.cms.mapper.TeamMemberMapper;
import com.cms.mapper.TeamRegistrationMapper;
import com.cms.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student/registration")
@PreAuthorize("hasRole('STUDENT')")
public class StudentRegistrationController {

    @Autowired private RegistrationService registrationService;
    @Autowired private RegistrationMapper registrationMapper;
    @Autowired private TeamRegistrationMapper teamRegistrationMapper;
    @Autowired private TeamMemberMapper teamMemberMapper;
    @Autowired private TeamMapper teamMapper;

    /** 个人赛报名 */
    @PostMapping
    public Result<Registration> register(@RequestParam Long competitionId,
                                          @RequestParam(required = false) String description,
                                          @RequestParam(required = false) String attachment) {
        return Result.success(registrationService.registerIndividual(
            competitionId, SecurityUtil.currentUserId(), description, attachment));
    }

    /** 取消报名 */
    @DeleteMapping("/{id}")
    public Result cancel(@PathVariable Long id) {
        registrationService.cancelRegistration(id, SecurityUtil.currentUserId());
        return Result.success();
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
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Integer status) {
        Page<TeamRegistration> page = new Page<>(pageNum, pageSize);
        // 查询当前用户所在的团队ID列表（排除已解散的团队）
        List<Long> teamIds = teamMemberMapper.selectList(
            new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getUserId, SecurityUtil.currentUserId())
                .eq(TeamMember::getStatus, 1)
        ).stream().map(TeamMember::getTeamId).collect(Collectors.toList());
        if (teamIds.isEmpty()) {
            return Result.success(new Page<>(pageNum, pageSize));
        }
        // 过滤掉已解散的团队
        List<Long> activeTeamIds = teamMapper.selectList(
            new LambdaQueryWrapper<Team>()
                .in(Team::getId, teamIds)
                .isNull(Team::getDissolvedAt)
        ).stream().map(Team::getId).collect(Collectors.toList());
        if (activeTeamIds.isEmpty()) {
            return Result.success(new Page<>(pageNum, pageSize));
        }
        LambdaQueryWrapper<TeamRegistration> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(TeamRegistration::getTeamId, activeTeamIds);
        if (status != null) {
            wrapper.eq(TeamRegistration::getStatus, status);
        }
        wrapper.orderByDesc(TeamRegistration::getRegisterTime);
        return Result.success(teamRegistrationMapper.selectPage(page, wrapper));
    }
}

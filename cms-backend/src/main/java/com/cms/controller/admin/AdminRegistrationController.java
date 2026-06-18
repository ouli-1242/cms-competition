package com.cms.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.entity.Competition;
import com.cms.entity.Registration;
import com.cms.entity.Team;
import com.cms.entity.TeamMember;
import com.cms.entity.TeamRegistration;
import com.cms.entity.User;
import com.cms.mapper.CompetitionMapper;
import com.cms.mapper.RegistrationMapper;
import com.cms.mapper.TeamMapper;
import com.cms.mapper.TeamMemberMapper;
import com.cms.mapper.TeamRegistrationMapper;
import com.cms.mapper.UserMapper;
import com.cms.service.RegistrationService;
import com.cms.service.TeamRegistrationService;
import com.cms.vo.RegistrationVO;
import com.cms.vo.TeamRegistrationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRegistrationController {

    @Autowired private RegistrationService registrationService;
    @Autowired private TeamRegistrationService teamRegistrationService;
    @Autowired private RegistrationMapper registrationMapper;
    @Autowired private TeamRegistrationMapper teamRegistrationMapper;
    @Autowired private UserMapper userMapper;
    @Autowired private CompetitionMapper competitionMapper;
    @Autowired private TeamMapper teamMapper;
    @Autowired private TeamMemberMapper teamMemberMapper;

    @GetMapping("/registration/page")
    public Result<Page<RegistrationVO>> registrationPage(
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
            LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
            userQuery.like(User::getRealName, studentName).select(User::getId);
            List<Long> userIds = userMapper.selectList(userQuery).stream().map(User::getId).collect(Collectors.toList());
            if (userIds.isEmpty()) userIds.add(-1L);
            wrapper.in(Registration::getUserId, userIds);
        }
        wrapper.orderByDesc(Registration::getRegisterTime);
        Page<Registration> result = registrationMapper.selectPage(page, wrapper);

        // Enrich with student name and competition title
        Map<Long, String> compMap = new HashMap<>();
        competitionMapper.selectList(null).forEach(c -> compMap.put(c.getId(), c.getTitle()));
        Set<Long> userIds = result.getRecords().stream().map(Registration::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }
        List<RegistrationVO> voList = result.getRecords().stream().map(r -> {
            RegistrationVO vo = new RegistrationVO();
            BeanUtils.copyProperties(r, vo);
            User u = userMap.get(r.getUserId());
            if (u != null) {
                vo.setStudentName(u.getRealName());
                vo.setCollege(u.getCollege());
            }
            vo.setCompetitionName(compMap.get(r.getCompetitionId()));
            return vo;
        }).collect(Collectors.toList());

        Page<RegistrationVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return Result.success(voPage);
    }

    @PutMapping("/registration/{id}/review")
    public Result reviewRegistration(@PathVariable Long id,
                                      @RequestParam Boolean pass,
                                      @RequestParam(required = false) String remark) {
        registrationService.review(id, pass, remark, SecurityUtil.currentUserId());
        return Result.success();
    }

    @GetMapping("/team-registration/page")
    public Result<Page<TeamRegistrationVO>> teamRegPage(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Long competitionId,
        @RequestParam(required = false) Integer status) {
        Page<TeamRegistration> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<TeamRegistration> wrapper = new LambdaQueryWrapper<>();
        if (competitionId != null) wrapper.eq(TeamRegistration::getCompetitionId, competitionId);
        if (status != null) wrapper.eq(TeamRegistration::getStatus, status);
        wrapper.orderByDesc(TeamRegistration::getRegisterTime);
        Page<TeamRegistration> result = teamRegistrationMapper.selectPage(page, wrapper);

        // Batch load related data
        Set<Long> teamIds = result.getRecords().stream().map(TeamRegistration::getTeamId).collect(Collectors.toSet());
        Set<Long> compIds = result.getRecords().stream().map(TeamRegistration::getCompetitionId).collect(Collectors.toSet());

        Map<Long, String> compMap = new HashMap<>();
        if (!compIds.isEmpty()) {
            competitionMapper.selectBatchIds(compIds).forEach(c -> compMap.put(c.getId(), c.getTitle()));
        }
        Map<Long, Team> teamMap = new HashMap<>();
        if (!teamIds.isEmpty()) {
            teamMapper.selectBatchIds(teamIds).forEach(t -> teamMap.put(t.getId(), t));
        }
        // Get captains and member counts
        Map<Long, String> captainMap = new HashMap<>();
        Map<Long, Long> memberCountMap = new HashMap<>();
        if (!teamIds.isEmpty()) {
            LambdaQueryWrapper<TeamMember> memberQuery = new LambdaQueryWrapper<>();
            memberQuery.in(TeamMember::getTeamId, teamIds).eq(TeamMember::getStatus, 1);
            List<TeamMember> allMembers = teamMemberMapper.selectList(memberQuery);
            Set<Long> captainIds = new HashSet<>();
            for (TeamMember m : allMembers) {
                memberCountMap.merge(m.getTeamId(), 1L, Long::sum);
                if (m.getRole() != null && m.getRole() == 1) {
                    captainIds.add(m.getUserId());
                }
            }
            if (!captainIds.isEmpty()) {
                userMapper.selectBatchIds(captainIds).forEach(u -> captainMap.put(u.getId(), u.getRealName()));
            }
        }

        List<TeamRegistrationVO> voList = result.getRecords().stream().map(tr -> {
            TeamRegistrationVO vo = new TeamRegistrationVO();
            BeanUtils.copyProperties(tr, vo);
            vo.setCompetitionName(compMap.get(tr.getCompetitionId()));
            Team team = teamMap.get(tr.getTeamId());
            if (team != null) {
                vo.setTeamName(team.getTeamName());
                vo.setMaxSize(team.getMaxSize());
                if (team.getCaptainId() != null) {
                    vo.setCaptainName(captainMap.get(team.getCaptainId()));
                }
            }
            vo.setMemberCount(memberCountMap.getOrDefault(tr.getTeamId(), 0L).intValue());
            return vo;
        }).collect(Collectors.toList());

        Page<TeamRegistrationVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(voList);
        return Result.success(voPage);
    }

    @PutMapping("/team-registration/{id}/review")
    public Result reviewTeamReg(@PathVariable Long id,
                                 @RequestParam Boolean pass,
                                 @RequestParam(required = false) String remark) {
        teamRegistrationService.review(id, pass, remark, SecurityUtil.currentUserId());
        return Result.success();
    }
}

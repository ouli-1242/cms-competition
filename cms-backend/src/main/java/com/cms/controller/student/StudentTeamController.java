package com.cms.controller.student;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cms.common.exception.BusinessException;
import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.dto.TeamDTO;
import com.cms.entity.Team;
import com.cms.entity.TeamAdvisor;
import com.cms.entity.User;
import com.cms.mapper.TeamAdvisorMapper;
import com.cms.mapper.UserMapper;
import com.cms.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student/team")
@PreAuthorize("hasRole('STUDENT')")
public class StudentTeamController {

    @Autowired private TeamService teamService;
    @Autowired private TeamAdvisorMapper teamAdvisorMapper;
    @Autowired private UserMapper userMapper;

    /** 创建团队 */
    @PostMapping
    public Result<Team> create(@Valid @RequestBody TeamDTO dto) {
        return Result.success(teamService.createTeam(dto, SecurityUtil.currentUserId()));
    }

    /** 加入团队（通过邀请码） */
    @PostMapping("/join")
    public Result join(@RequestParam String inviteCode) {
        teamService.joinTeam(inviteCode, SecurityUtil.currentUserId());
        return Result.success();
    }


    /** 我的团队（列表） */
    @GetMapping("/my")
    public Result<List<Map<String, Object>>> myTeam() {
        return Result.success(teamService.getMyTeam(SecurityUtil.currentUserId()));
    }

    /** 团队详情 */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        return Result.success(teamService.getTeamDetail(id));
    }


    /** 退出团队 */
    @PostMapping("/{id}/quit")
    public Result quit(@PathVariable Long id) {
        teamService.quitTeam(id, SecurityUtil.currentUserId());
        return Result.success();
    }


    /** 解散团队（仅队长可操作） */
    @PostMapping("/{id}/dissolve")
    public Result dissolve(@PathVariable Long id) {
        teamService.dissolveTeam(id, SecurityUtil.currentUserId());
        return Result.success();
    }


    /** 恢复已解散的团队（12小时内，仅队长） */
    @PostMapping("/{id}/recover")
    public Result recover(@PathVariable Long id) {
        teamService.recoverTeam(id, SecurityUtil.currentUserId());
        return Result.success();
    }


    /** 审核成员 */
    @PutMapping("/review")
    public Result review(@RequestParam Long memberId, @RequestParam Boolean pass) {
        teamService.reviewMember(memberId, pass, SecurityUtil.currentUserId());
        return Result.success();
    }

    /** 踢出成员 */
    @PutMapping("/kick")
    public Result kick(@RequestParam Long teamId, @RequestParam Long userId) {
        teamService.kickMember(teamId, userId, SecurityUtil.currentUserId());
        return Result.success();
    }


    /** 转让队长 */
    @PutMapping("/transfer")
    public Result transfer(@RequestParam Long teamId, @RequestParam Long newCaptainId) {
        teamService.transferCaptain(teamId, newCaptainId, SecurityUtil.currentUserId());
        return Result.success();
    }


    /** 提交团队报名 */
    @PostMapping("/submit")
    public Result submit(@RequestParam Long teamId,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) String attachment) {
        teamService.submitTeamRegistration(teamId, description, attachment, SecurityUtil.currentUserId());
        return Result.success();
    }

    /** 搜索学生（模糊匹配姓名/学院） */
    @GetMapping("/search-students")
    public Result<List<Map<String, Object>>> searchStudents(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "STUDENT");
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getRealName, keyword).or().like(User::getCollege, keyword));
        }
        wrapper.select(User::getId, User::getRealName, User::getCollege, User::getAvatar);
        List<User> students = userMapper.selectList(wrapper);
        List<Map<String, Object>> result = students.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("realName", s.getRealName());
            m.put("college", s.getCollege());
            m.put("avatar", s.getAvatar());
            return m;
        }).collect(Collectors.toList());
        return Result.success(result);
    }


    /** 搜索教师（模糊匹配姓名/学院） */
    @GetMapping("/search-teachers")
    public Result<List<Map<String, Object>>> searchTeachers(@RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "TEACHER");
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getRealName, keyword).or().like(User::getCollege, keyword));
        }
        wrapper.select(User::getId, User::getRealName, User::getCollege, User::getAvatar);
        List<User> teachers = userMapper.selectList(wrapper);
        List<Map<String, Object>> result = teachers.stream().map(t -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", t.getId());
            m.put("realName", t.getRealName());
            m.put("college", t.getCollege());
            m.put("avatar", t.getAvatar());
            return m;
        }).collect(Collectors.toList());
        return Result.success(result);
    }


    /** 邀请指导老师 */
    @PostMapping("/{teamId}/invite-teacher")
    public Result inviteTeacher(@PathVariable Long teamId,
                                @RequestParam Long teacherId,
                                @RequestParam(required = false) String remark) {
        Long userId = SecurityUtil.currentUserId();
        Team team = teamService.getById(teamId);
        if (team == null) throw new BusinessException("团队不存在");
        if (!team.getCaptainId().equals(userId)) throw new BusinessException("仅队长可邀请指导老师");
        if (team.getDissolvedAt() != null) throw new BusinessException("团队已解散");

        // 校验教师存在
        User teacher = userMapper.selectById(teacherId);
        if (teacher == null || !"TEACHER".equals(teacher.getRole())) throw new BusinessException("教师不存在");

        // 校验：不能重复邀请同一教师
        Long dupCount = teamAdvisorMapper.selectCount(new LambdaQueryWrapper<TeamAdvisor>()
            .eq(TeamAdvisor::getTeamId, teamId)
            .eq(TeamAdvisor::getTeacherId, teacherId)
            .in(TeamAdvisor::getStatus, 0, 1));
        if (dupCount > 0) throw new BusinessException("该教师已是/正在审核中");

        // 校验：一个团队只能有一位指导老师
        Long existingAccepted = teamAdvisorMapper.selectCount(new LambdaQueryWrapper<TeamAdvisor>()
            .eq(TeamAdvisor::getTeamId, teamId)
            .eq(TeamAdvisor::getStatus, 1));
        if (existingAccepted > 0) throw new BusinessException("团队已有指导老师");

        TeamAdvisor advisor = new TeamAdvisor();
        advisor.setTeamId(teamId);
        advisor.setTeacherId(teacherId);
        advisor.setStatus(0);
        advisor.setInvitedBy(userId);
        advisor.setInviteTime(LocalDateTime.now());
        advisor.setRemark(remark);
        teamAdvisorMapper.insert(advisor);
        return Result.success();
    }

    /** 团队邀请记录列表 */
    @GetMapping("/{teamId}/advisor-invitations")
    public Result<List<Map<String, Object>>> getInvitations(@PathVariable Long teamId) {
        Long userId = SecurityUtil.currentUserId();
        Team team = teamService.getById(teamId);
        if (team == null) throw new BusinessException("团队不存在");
        if (!team.getCaptainId().equals(userId)) throw new BusinessException("仅队长可查看");

        List<TeamAdvisor> list = teamAdvisorMapper.selectList(new LambdaQueryWrapper<TeamAdvisor>()
            .eq(TeamAdvisor::getTeamId, teamId)
            .orderByDesc(TeamAdvisor::getInviteTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (TeamAdvisor a : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", a.getId());
            m.put("teacherId", a.getTeacherId());
            m.put("status", a.getStatus());
            m.put("remark", a.getRemark());
            m.put("inviteTime", a.getInviteTime());
            m.put("acceptTime", a.getAcceptTime());
            m.put("rejectTime", a.getRejectTime());
            User teacher = userMapper.selectById(a.getTeacherId());
            m.put("teacherName", teacher != null ? teacher.getRealName() : "未知");
            m.put("teacherCollege", teacher != null ? teacher.getCollege() : "");
            result.add(m);
        }
        return Result.success(result);
    }

    /** 撤销待审邀请 */
    @DeleteMapping("/advisor-invite/{inviteId}")
    public Result cancelInvite(@PathVariable Long inviteId) {
        Long userId = SecurityUtil.currentUserId();
        TeamAdvisor advisor = teamAdvisorMapper.selectById(inviteId);
        if (advisor == null) throw new BusinessException("邀请不存在");
        if (advisor.getStatus() != 0) throw new BusinessException("只能撤销待审核的邀请");
        Team team = teamService.getById(advisor.getTeamId());
        if (team == null || !team.getCaptainId().equals(userId)) throw new BusinessException("无权操作");
        teamAdvisorMapper.deleteById(inviteId);
        return Result.success();
    }
}

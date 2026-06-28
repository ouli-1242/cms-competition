package com.cms.controller.student;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.entity.Competition;
import com.cms.entity.Registration;
import com.cms.entity.Team;
import com.cms.entity.TeamMember;
import com.cms.entity.TeamRegistration;
import com.cms.mapper.CompetitionMapper;
import com.cms.mapper.RegistrationMapper;
import com.cms.mapper.TeamMapper;
import com.cms.mapper.TeamMemberMapper;
import com.cms.mapper.TeamRegistrationMapper;
import com.cms.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
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
    @Autowired private CompetitionMapper competitionMapper;

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
    public Result myTeamPage(
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
            Map<String, Object> empty = new HashMap<>();
            empty.put("records", Collections.emptyList());
            empty.put("total", 0L);
            empty.put("pages", 0L);
            empty.put("current", (long) pageNum);
            empty.put("size", (long) pageSize);
            return Result.success(empty);
        }
        // 过滤掉已解散的团队
        List<Long> activeTeamIds = teamMapper.selectList(
            new LambdaQueryWrapper<Team>()
                .in(Team::getId, teamIds)
                .isNull(Team::getDissolvedAt)
        ).stream().map(Team::getId).collect(Collectors.toList());
        if (activeTeamIds.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("records", Collections.emptyList());
            empty.put("total", 0L);
            empty.put("pages", 0L);
            empty.put("current", (long) pageNum);
            empty.put("size", (long) pageSize);
            return Result.success(empty);
        }
        LambdaQueryWrapper<TeamRegistration> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(TeamRegistration::getTeamId, activeTeamIds);
        if (status != null) {
            wrapper.eq(TeamRegistration::getStatus, status);
        }
        wrapper.orderByDesc(TeamRegistration::getRegisterTime);
        Page<TeamRegistration> result = teamRegistrationMapper.selectPage(page, wrapper);

        // 批量加载团队名称
        Set<Long> resultTeamIds = result.getRecords().stream()
            .map(TeamRegistration::getTeamId).collect(Collectors.toSet());
        Map<Long, String> teamNameMap = new HashMap<>();
        if (!resultTeamIds.isEmpty()) {
            teamMapper.selectBatchIds(resultTeamIds).forEach(t -> teamNameMap.put(t.getId(), t.getTeamName()));
        }

        // 构建带团队名称的响应
        List<Map<String, Object>> enriched = result.getRecords().stream().map(tr -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", tr.getId());
            m.put("competitionId", tr.getCompetitionId());
            m.put("teamId", tr.getTeamId());
            m.put("teamName", teamNameMap.getOrDefault(tr.getTeamId(), ""));
            m.put("status", tr.getStatus());
            m.put("attachment", tr.getAttachment());
            m.put("description", tr.getDescription());
            m.put("registerTime", tr.getRegisterTime());
            m.put("reviewRemark", tr.getReviewRemark());
            m.put("reviewTime", tr.getReviewTime());
            return m;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("records", enriched);
        response.put("total", result.getTotal());
        response.put("pages", result.getPages());
        response.put("current", result.getCurrent());
        response.put("size", result.getSize());
        return Result.success(response);
    }


    /** 报名详情（含竞赛名称） */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        Registration reg = registrationMapper.selectById(id);
        if (reg == null) {
            return Result.error("报名记录不存在");
        }
        if (!reg.getUserId().equals(SecurityUtil.currentUserId())) {
            return Result.error("无权查看");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", reg.getId());
        map.put("competitionId", reg.getCompetitionId());
        map.put("status", reg.getStatus());
        map.put("description", reg.getDescription());
        map.put("attachment", reg.getAttachment());
        map.put("registerTime", reg.getRegisterTime());
        map.put("reviewRemark", reg.getReviewRemark());
        map.put("reviewTime", reg.getReviewTime());
        Competition comp = competitionMapper.selectById(reg.getCompetitionId());
        map.put("competitionTitle", comp != null ? comp.getTitle() : "");
        map.put("competitionType", comp != null ? comp.getType() : null);
        return Result.success(map);
    }


    /** 编辑报名（仅待审核状态可改） */
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id,
                         @RequestParam(required = false) String description,
                         @RequestParam(required = false) String attachment) {
        Registration reg = registrationMapper.selectById(id);
        if (reg == null) {
            return Result.error("报名记录不存在");
        }
        if (!reg.getUserId().equals(SecurityUtil.currentUserId())) {
            return Result.error("无权操作");
        }
        if (reg.getStatus() != 0) {
            return Result.error("仅待审核状态可编辑");
        }
        if (description != null) reg.setDescription(description);
        if (attachment != null) reg.setAttachment(attachment);
        registrationMapper.updateById(reg);
        return Result.success();
    }


    /** 团队报名详情 */
    @GetMapping("/team/{id}")
    public Result<Map<String, Object>> getTeamRegDetail(@PathVariable Long id) {
        TeamRegistration tr = teamRegistrationMapper.selectById(id);
        if (tr == null) {
            return Result.error("团队报名记录不存在");
        }
        // 校验当前用户是否在该团队中
        TeamMember member = teamMemberMapper.selectOne(
            new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, tr.getTeamId())
                .eq(TeamMember::getUserId, SecurityUtil.currentUserId())
                .eq(TeamMember::getStatus, 1)
        );
        if (member == null) {
            return Result.error("无权查看");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("id", tr.getId());
        map.put("competitionId", tr.getCompetitionId());
        map.put("teamId", tr.getTeamId());
        map.put("status", tr.getStatus());
        map.put("description", tr.getDescription());
        map.put("attachment", tr.getAttachment());
        map.put("registerTime", tr.getRegisterTime());
        map.put("reviewRemark", tr.getReviewRemark());
        map.put("reviewTime", tr.getReviewTime());
        map.put("isCaptain", member.getRole() != null && member.getRole() == 1);
        Competition comp = competitionMapper.selectById(tr.getCompetitionId());
        map.put("competitionTitle", comp != null ? comp.getTitle() : "");
        Team team = teamMapper.selectById(tr.getTeamId());
        map.put("teamName", team != null ? team.getTeamName() : "");
        return Result.success(map);
    }


    /** 编辑团队报名（仅队长+待审核状态可改） */
    @PutMapping("/team/{id}")
    public Result updateTeam(@PathVariable Long id,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String attachment) {
        TeamRegistration tr = teamRegistrationMapper.selectById(id);
        if (tr == null) {
            return Result.error("团队报名记录不存在");
        }
        TeamMember member = teamMemberMapper.selectOne(
            new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, tr.getTeamId())
                .eq(TeamMember::getUserId, SecurityUtil.currentUserId())
                .eq(TeamMember::getStatus, 1)
        );
        if (member == null) {
            return Result.error("无权操作");
        }
        if (member.getRole() == null || member.getRole() != 1) {
            return Result.error("仅队长可编辑");
        }
        if (tr.getStatus() != 0) {
            return Result.error("仅待审核状态可编辑");
        }
        if (description != null) tr.setDescription(description);
        if (attachment != null) tr.setAttachment(attachment);
        teamRegistrationMapper.updateById(tr);
        return Result.success();
    }
}

package com.cms.controller.teacher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cms.common.exception.BusinessException;
import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.entity.*;
import com.cms.mapper.*;
import com.cms.vo.AdvisorInvitationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teacher/advisor")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherAdvisorController {

    @Autowired private TeamAdvisorMapper teamAdvisorMapper;
    @Autowired private TeamMapper teamMapper;
    @Autowired private TeamMemberMapper teamMemberMapper;
    @Autowired private CompetitionMapper competitionMapper;
    @Autowired private UserMapper userMapper;

    /** 我的指导邀请列表 */
    @GetMapping("/invitations")
    public Result<List<AdvisorInvitationVO>> invitations(@RequestParam(required = false) Integer status) {
        Long teacherId = SecurityUtil.currentUserId();
        LambdaQueryWrapper<TeamAdvisor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamAdvisor::getTeacherId, teacherId);
        if (status != null) wrapper.eq(TeamAdvisor::getStatus, status);
        wrapper.orderByDesc(TeamAdvisor::getInviteTime);
        List<TeamAdvisor> list = teamAdvisorMapper.selectList(wrapper);

        // batch enrich
        Set<Long> teamIds = list.stream().map(TeamAdvisor::getTeamId).collect(Collectors.toSet());
        Set<Long> userIds = list.stream().map(TeamAdvisor::getInvitedBy).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Team> teamMap = teamIds.isEmpty() ? Collections.emptyMap() :
            teamMapper.selectBatchIds(teamIds).stream().collect(Collectors.toMap(Team::getId, t -> t));
        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap() :
            userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));
        Set<Long> compIds = teamMap.values().stream().map(Team::getCompetitionId).collect(Collectors.toSet());
        Map<Long, Competition> compMap = compIds.isEmpty() ? Collections.emptyMap() :
            competitionMapper.selectBatchIds(compIds).stream().collect(Collectors.toMap(Competition::getId, c -> c));

        User teacher = userMapper.selectById(teacherId);

        List<AdvisorInvitationVO> result = list.stream().map(a -> {
            AdvisorInvitationVO vo = new AdvisorInvitationVO();
            vo.setId(a.getId());
            vo.setTeamId(a.getTeamId());
            vo.setTeacherId(a.getTeacherId());
            vo.setStatus(a.getStatus());
            vo.setInvitedBy(a.getInvitedBy());
            vo.setInviteTime(a.getInviteTime());
            vo.setAcceptTime(a.getAcceptTime());
            vo.setRejectTime(a.getRejectTime());
            vo.setRemark(a.getRemark());

            Team team = teamMap.get(a.getTeamId());
            vo.setTeamName(team != null ? team.getTeamName() : "未知团队");
            Competition comp = team != null ? compMap.get(team.getCompetitionId()) : null;
            vo.setCompetitionName(comp != null ? comp.getTitle() : "");
            User inviter = userMap.get(a.getInvitedBy());
            vo.setInviterName(inviter != null ? inviter.getRealName() : "未知");
            vo.setTeacherName(teacher != null ? teacher.getRealName() : "");
            return vo;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    /** 接受邀请 */
    @PostMapping("/{inviteId}/accept")
    public Result accept(@PathVariable Long inviteId) {
        Long teacherId = SecurityUtil.currentUserId();
        TeamAdvisor advisor = teamAdvisorMapper.selectById(inviteId);
        if (advisor == null) throw new BusinessException("邀请不存在");
        if (!advisor.getTeacherId().equals(teacherId)) throw new BusinessException("无权操作");
        if (advisor.getStatus() != 0) throw new BusinessException("邀请已处理");

        // 检查该团队是否已有指导老师
        Long existing = teamAdvisorMapper.selectCount(new LambdaQueryWrapper<TeamAdvisor>()
            .eq(TeamAdvisor::getTeamId, advisor.getTeamId())
            .eq(TeamAdvisor::getStatus, 1));
        if (existing > 0) throw new BusinessException("该团队已有指导老师");

        advisor.setStatus(1);
        advisor.setAcceptTime(LocalDateTime.now());
        teamAdvisorMapper.updateById(advisor);
        return Result.success();
    }

    /** 拒绝邀请 */
    @PostMapping("/{inviteId}/reject")
    public Result reject(@PathVariable Long inviteId) {
        Long teacherId = SecurityUtil.currentUserId();
        TeamAdvisor advisor = teamAdvisorMapper.selectById(inviteId);
        if (advisor == null) throw new BusinessException("邀请不存在");
        if (!advisor.getTeacherId().equals(teacherId)) throw new BusinessException("无权操作");
        if (advisor.getStatus() != 0) throw new BusinessException("邀请已处理");

        advisor.setStatus(2);
        advisor.setRejectTime(LocalDateTime.now());
        teamAdvisorMapper.updateById(advisor);
        return Result.success();
    }

    /** 我指导的团队列表 */
    @GetMapping("/teams")
    public Result<List<Map<String, Object>>> advisedTeams() {
        Long teacherId = SecurityUtil.currentUserId();
        List<TeamAdvisor> accepted = teamAdvisorMapper.selectList(new LambdaQueryWrapper<TeamAdvisor>()
            .eq(TeamAdvisor::getTeacherId, teacherId)
            .eq(TeamAdvisor::getStatus, 1));
        if (accepted.isEmpty()) return Result.success(Collections.emptyList());

        List<Long> teamIds = accepted.stream().map(TeamAdvisor::getTeamId).collect(Collectors.toList());
        List<Team> teams = teamMapper.selectBatchIds(teamIds);
        Set<Long> compIds = teams.stream().map(Team::getCompetitionId).collect(Collectors.toSet());
        Map<Long, Competition> compMap = compIds.isEmpty() ? Collections.emptyMap() :
            competitionMapper.selectBatchIds(compIds).stream().collect(Collectors.toMap(Competition::getId, c -> c));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Team team : teams) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", team.getId());
            m.put("teamName", team.getTeamName());
            m.put("competitionId", team.getCompetitionId());
            Competition comp = compMap.get(team.getCompetitionId());
            m.put("competitionName", comp != null ? comp.getTitle() : "");
            m.put("status", team.getStatus());
            m.put("maxSize", team.getMaxSize());
            // 成员数
            Long memberCount = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, team.getId())
                .eq(TeamMember::getStatus, 1));
            m.put("memberCount", memberCount);
            // 队长名
            User captain = userMapper.selectById(team.getCaptainId());
            m.put("captainName", captain != null ? captain.getRealName() : "");
            result.add(m);
        }
        return Result.success(result);
    }
}

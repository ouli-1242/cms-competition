package com.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cms.common.exception.BusinessException;
import com.cms.dto.TeamDTO;
import com.cms.entity.Competition;
import com.cms.entity.Team;
import com.cms.entity.TeamAdvisor;
import com.cms.entity.TeamMember;
import com.cms.entity.TeamRegistration;
import com.cms.entity.User;
import com.cms.mapper.*;
import com.cms.service.NotificationService;
import com.cms.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    @Autowired private TeamMemberMapper teamMemberMapper;
    @Autowired private CompetitionMapper competitionMapper;
    @Autowired private TeamRegistrationMapper teamRegistrationMapper;
    @Autowired private NotificationService notificationService;
    @Autowired private UserMapper userMapper;
    @Autowired private TeamAdvisorMapper teamAdvisorMapper;

    @Override
    @Transactional
    public Team createTeam(TeamDTO dto, Long captainId) {
        Competition comp = competitionMapper.selectById(dto.getCompetitionId());
        if (comp == null) throw new BusinessException("竞赛不存在");
        if (comp.getStatus() != 1) throw new BusinessException("竞赛未上架");
        if (comp.getType() == null || comp.getType() != 2) throw new BusinessException("该竞赛不支持团队赛");
        LocalDateTime now = LocalDateTime.now();
        if (comp.getRegisterStart() != null && now.isBefore(comp.getRegisterStart()))
            throw new BusinessException("报名尚未开始");
        if (comp.getRegisterEnd() != null && now.isAfter(comp.getRegisterEnd()))
            throw new BusinessException("报名已结束");

        // 校验：同一竞赛只能当一个队的队长
        Long captainCount = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
            .eq(TeamMember::getUserId, captainId)
            .eq(TeamMember::getRole, 1)
            .inSql(TeamMember::getTeamId,
                "SELECT id FROM team WHERE competition_id = " + dto.getCompetitionId() + " AND is_deleted = 0 AND dissolved_at IS NULL"));
        if (captainCount > 0) throw new BusinessException("您在该竞赛中已是其他团队的队长");

        Team team = new Team();
        team.setTeamName(dto.getTeamName());
        team.setCompetitionId(dto.getCompetitionId());
        team.setCaptainId(captainId);
        team.setMaxSize(dto.getMaxSize() == null ? 5 : dto.getMaxSize());
        team.setSlogan(dto.getSlogan());
        team.setInviteCode(generateInviteCode());
        team.setStatus(0);
        this.save(team);

        // 队长自动加入
        TeamMember m = new TeamMember();
        m.setTeamId(team.getId());
        m.setUserId(captainId);
        m.setRole(1);
        m.setStatus(1);
        m.setJoinTime(LocalDateTime.now());
        teamMemberMapper.insert(m);

        // 创建时邀请学生（待审核）
        if (dto.getStudentIds() != null && !dto.getStudentIds().isEmpty()) {
            for (Long studentId : dto.getStudentIds()) {
                if (studentId.equals(captainId)) continue;
                // 跳过已存在记录
                Long existCount = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
                    .eq(TeamMember::getTeamId, team.getId())
                    .eq(TeamMember::getUserId, studentId));
                if (existCount > 0) continue;
                TeamMember invited = new TeamMember();
                invited.setTeamId(team.getId());
                invited.setUserId(studentId);
                invited.setRole(0);
                invited.setStatus(0);
                invited.setJoinTime(LocalDateTime.now());
                teamMemberMapper.insert(invited);
                notificationService.asyncNotify(studentId,
                    "您被邀请加入团队「" + team.getTeamName() + "」，请前往我的团队查看", "TEAM");
            }
        }

        // 创建时邀请指导老师（待审核）
        if (dto.getTeacherId() != null) {
            User teacher = userMapper.selectById(dto.getTeacherId());
            if (teacher != null && "TEACHER".equals(teacher.getRole())) {
                TeamAdvisor advisor = new TeamAdvisor();
                advisor.setTeamId(team.getId());
                advisor.setTeacherId(dto.getTeacherId());
                advisor.setStatus(0);
                advisor.setInvitedBy(captainId);
                advisor.setInviteTime(LocalDateTime.now());
                teamAdvisorMapper.insert(advisor);
                notificationService.asyncNotify(dto.getTeacherId(),
                    "团队「" + team.getTeamName() + "」邀请您担任指导老师，请前往指导邀请查看", "TEAM");
            }
        }

        return team;
    }


    @Override
    @Transactional
    public void joinTeam(String inviteCode, Long userId) {
        Team team = this.getOne(new LambdaQueryWrapper<Team>().eq(Team::getInviteCode, inviteCode));
        if (team == null) throw new BusinessException("邀请码无效");
        if (team.getStatus() != 0) throw new BusinessException("团队已锁定");
        if (team.getDissolvedAt() != null) throw new BusinessException("团队已解散");

        // 重复检查
        Long count = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
            .eq(TeamMember::getTeamId, team.getId())
            .eq(TeamMember::getUserId, userId));
        if (count > 0) throw new BusinessException("您已申请/加入该团队");

        // 人数检查
        Long memberCount = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
            .eq(TeamMember::getTeamId, team.getId())
            .eq(TeamMember::getStatus, 1));
        if (memberCount >= team.getMaxSize()) throw new BusinessException("团队已满");

        TeamMember m = new TeamMember();
        m.setTeamId(team.getId());
        m.setUserId(userId);
        m.setRole(0);
        m.setStatus(0);  // 待队长审核
        m.setJoinTime(LocalDateTime.now());
        teamMemberMapper.insert(m);

        // 通知队长审核
        notificationService.asyncNotify(team.getCaptainId(), "有新成员申请加入您的团队，请及时审核", "TEAM");
    }


    @Override
    public List<Map<String, Object>> getMyTeam(Long userId) {
        List<TeamMember> memberships = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
            .eq(TeamMember::getUserId, userId)
            .in(TeamMember::getStatus, 0, 1));
        List<Map<String, Object>> result = new ArrayList<>();
        for (TeamMember m : memberships) {
            Team team = this.getById(m.getTeamId());
            if (team == null || team.getDissolvedAt() != null) continue;
            List<TeamMember> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, team.getId())
                .eq(TeamMember::getStatus, 1));
            Map<String, Object> map = new HashMap<>();
            map.put("team", team);
            map.put("members", members);
            map.put("myRole", m.getRole());
            map.put("memberStatus", m.getStatus());

            // 团队报名审核状态
            TeamRegistration reg = teamRegistrationMapper.selectOne(new LambdaQueryWrapper<TeamRegistration>()
                .eq(TeamRegistration::getTeamId, team.getId())
                .eq(TeamRegistration::getCompetitionId, team.getCompetitionId())
                .orderByDesc(TeamRegistration::getRegisterTime)
                .last("LIMIT 1"));
            map.put("registrationStatus", reg != null ? reg.getStatus() : null);

            result.add(map);
        }
        return result;
    }


    @Override
    public void reviewMember(Long memberId, Boolean pass, Long callerId) {
        TeamMember m = teamMemberMapper.selectById(memberId);
        if (m == null) throw new BusinessException("记录不存在");
        Team team = this.getById(m.getTeamId());
        if (team == null) throw new BusinessException("团队不存在");
        if (!team.getCaptainId().equals(callerId)) throw new BusinessException("仅队长可审核成员");
        m.setStatus(pass ? 1 : 2);
        teamMemberMapper.updateById(m);
    }


    @Override
    @Transactional
    public void kickMember(Long teamId, Long userId, Long operatorId) {
        Team team = this.getById(teamId);
        if (team == null) throw new BusinessException("团队不存在");
        if (!team.getCaptainId().equals(operatorId)) throw new BusinessException("仅队长可踢出成员");
        if (team.getCaptainId().equals(userId)) throw new BusinessException("队长不能踢出自己，请先转让队长");
        teamMemberMapper.delete(new LambdaQueryWrapper<TeamMember>()
            .eq(TeamMember::getTeamId, teamId)
            .eq(TeamMember::getUserId, userId));
        notificationService.asyncNotify(userId, "您已被踢出团队 " + team.getTeamName(), "TEAM");
    }


    @Override
    @Transactional
    public void transferCaptain(Long teamId, Long newCaptainId, Long oldCaptainId) {
        Team team = this.getById(teamId);
        if (team == null) throw new BusinessException("团队不存在");
        if (!team.getCaptainId().equals(oldCaptainId)) throw new BusinessException("仅队长可转让");

        // 验证新队长是团队中的活跃成员
        Long newCaptainMemberCount = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
            .eq(TeamMember::getTeamId, teamId)
            .eq(TeamMember::getUserId, newCaptainId)
            .eq(TeamMember::getStatus, 1));
        if (newCaptainMemberCount == 0) throw new BusinessException("新队长必须是团队中的活跃成员");

        // 旧队长 -> 队员
        teamMemberMapper.update(new TeamMember(),
            new UpdateWrapper<TeamMember>()
                .eq("team_id", teamId).eq("user_id", oldCaptainId)
                .set("role", 0));
        // 新队长 -> 队长
        teamMemberMapper.update(new TeamMember(),
            new UpdateWrapper<TeamMember>()
                .eq("team_id", teamId).eq("user_id", newCaptainId)
                .set("role", 1));

        team.setCaptainId(newCaptainId);
        this.updateById(team);
        notificationService.asyncNotify(newCaptainId, "您已成为团队 " + team.getTeamName() + " 的队长", "TEAM");
    }


    @Override
    @Transactional
    public void submitTeamRegistration(Long teamId, String description, String attachment, Long callerId) {
        Team team = this.getById(teamId);
        if (team == null) throw new BusinessException("团队不存在");
        if (!team.getCaptainId().equals(callerId)) throw new BusinessException("仅队长可提交团队报名");
        if (team.getStatus() != 0) throw new BusinessException("团队已提交");

        // 校验人数
        Long memberCount = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
            .eq(TeamMember::getTeamId, teamId)
            .eq(TeamMember::getStatus, 1));
        Competition comp = competitionMapper.selectById(team.getCompetitionId());
        if (comp != null && comp.getMinTeamSize() != null && memberCount < comp.getMinTeamSize()) {
            throw new BusinessException("团队人数不足，最少需要 " + comp.getMinTeamSize() + " 人");
        }

        // 校验：是否已报名
        Long exists = teamRegistrationMapper.selectCount(new LambdaQueryWrapper<TeamRegistration>()
            .eq(TeamRegistration::getCompetitionId, team.getCompetitionId())
            .eq(TeamRegistration::getTeamId, teamId));
        if (exists > 0) throw new BusinessException("该团队已报名");

        TeamRegistration reg = new TeamRegistration();
        reg.setCompetitionId(team.getCompetitionId());
        reg.setTeamId(teamId);
        reg.setStatus(0);
        reg.setDescription(description);
        reg.setAttachment(attachment);
        reg.setRegisterTime(LocalDateTime.now());
        teamRegistrationMapper.insert(reg);

        team.setStatus(1);
        this.updateById(team);
    }

    @Override
    public Map<String, Object> getTeamDetail(Long teamId) {
        Team team = this.getById(teamId);
        if (team == null) return Collections.emptyMap();
        List<TeamMember> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
            .eq(TeamMember::getTeamId, teamId));
        // 关联用户信息
        List<Map<String, Object>> enrichedMembers = new ArrayList<>();
        for (TeamMember m : members) {
            User user = userMapper.selectById(m.getUserId());
            Map<String, Object> mm = new HashMap<>();
            mm.put("id", m.getId());
            mm.put("userId", m.getUserId());
            mm.put("role", m.getRole());
            mm.put("status", m.getStatus());
            mm.put("joinTime", m.getJoinTime());
            if (user != null) {
                mm.put("realName", user.getRealName());
                mm.put("username", user.getUsername());
                mm.put("college", user.getCollege());
                mm.put("avatar", user.getAvatar());
            }
            enrichedMembers.add(mm);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("team", team);
        map.put("members", enrichedMembers);

        // 指导老师信息
        TeamAdvisor advisor = teamAdvisorMapper.selectOne(new LambdaQueryWrapper<TeamAdvisor>()
            .eq(TeamAdvisor::getTeamId, teamId)
            .eq(TeamAdvisor::getStatus, 1));
        if (advisor != null) {
            User teacher = userMapper.selectById(advisor.getTeacherId());
            Map<String, Object> advisorMap = new HashMap<>();
            advisorMap.put("id", advisor.getId());
            advisorMap.put("teacherId", advisor.getTeacherId());
            advisorMap.put("teacherName", teacher != null ? teacher.getRealName() : "未知");
            advisorMap.put("teacherCollege", teacher != null ? teacher.getCollege() : "");
            advisorMap.put("acceptTime", advisor.getAcceptTime());
            map.put("advisor", advisorMap);
        }
        // 待审核的邀请
        TeamAdvisor pendingInvite = teamAdvisorMapper.selectOne(new LambdaQueryWrapper<TeamAdvisor>()
            .eq(TeamAdvisor::getTeamId, teamId)
            .eq(TeamAdvisor::getStatus, 0));
        if (pendingInvite != null) {
            User teacher = userMapper.selectById(pendingInvite.getTeacherId());
            Map<String, Object> pendingMap = new HashMap<>();
            pendingMap.put("id", pendingInvite.getId());
            pendingMap.put("teacherId", pendingInvite.getTeacherId());
            pendingMap.put("teacherName", teacher != null ? teacher.getRealName() : "未知");
            pendingMap.put("inviteTime", pendingInvite.getInviteTime());
            map.put("pendingInvite", pendingMap);
        }
        return map;
    }

    @Override
    @Transactional
    public void quitTeam(Long teamId, Long userId) {
        Team team = this.getById(teamId);
        if (team == null) throw new BusinessException("团队不存在");
        if (team.getCaptainId().equals(userId)) {
            // 队长退出 → 检查是否有其他成员可接替
            Long memberCount = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId)
                .eq(TeamMember::getStatus, 1)
                .ne(TeamMember::getUserId, userId));
            if (memberCount > 0) {
                throw new BusinessException("队长不能直接退出，请先转让队长");
            }
            // 无其他成员，删除团队
            teamMemberMapper.delete(new LambdaQueryWrapper<TeamMember>()
                .eq(TeamMember::getTeamId, teamId));
            this.removeById(teamId);
            return;
        }
        teamMemberMapper.delete(new LambdaQueryWrapper<TeamMember>()
            .eq(TeamMember::getTeamId, teamId)
            .eq(TeamMember::getUserId, userId));
    }

    @Override
    @Transactional
    public void dissolveTeam(Long teamId, Long userId) {
        Team team = this.getById(teamId);
        if (team == null) throw new BusinessException("团队不存在");
        if (!team.getCaptainId().equals(userId)) {
            throw new BusinessException("只有队长可以解散团队");
        }
        if (team.getDissolvedAt() != null) {
            throw new BusinessException("团队已解散");
        }
        team.setDissolvedAt(LocalDateTime.now());
        this.updateById(team);
    }


    @Override
    @Transactional
    public void recoverTeam(Long teamId, Long userId) {
        Team team = this.getById(teamId);
        if (team == null) throw new BusinessException("团队不存在");
        if (team.getDissolvedAt() == null) throw new BusinessException("团队未被解散");
        if (!team.getCaptainId().equals(userId)) {
            throw new BusinessException("只有队长可以恢复团队");
        }
        LocalDateTime deadline = team.getDissolvedAt().plusHours(12);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new BusinessException("已超过12小时恢复期限，无法恢复");
        }
        team.setDissolvedAt(null);
        this.updateById(team);
    }

    private String generateInviteCode() {
        for (int i = 0; i < 10; i++) {
            String code = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            Long count = this.count(new LambdaQueryWrapper<Team>().eq(Team::getInviteCode, code));
            if (count == 0) return code;
        }
        throw new BusinessException("邀请码生成失败，请重试");
    }

}

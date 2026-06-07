package com.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cms.common.exception.BusinessException;
import com.cms.dto.TeamDTO;
import com.cms.entity.Competition;
import com.cms.entity.Team;
import com.cms.entity.TeamMember;
import com.cms.entity.TeamRegistration;
import com.cms.mapper.*;
import com.cms.service.NotificationService;
import com.cms.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService {

    @Autowired private TeamMemberMapper teamMemberMapper;
    @Autowired private CompetitionMapper competitionMapper;
    @Autowired private TeamRegistrationMapper teamRegistrationMapper;
    @Autowired private NotificationService notificationService;

    @Override
    @Transactional
    public Team createTeam(TeamDTO dto, Long captainId) {
        Competition comp = competitionMapper.selectById(dto.getCompetitionId());
        if (comp == null) throw new BusinessException("竞赛不存在");
        if (comp.getStatus() != 1) throw new BusinessException("竞赛未上架");

        // 校验：当前用户是否已加入其他团队
        Long count = teamMemberMapper.selectCount(new LambdaQueryWrapper<TeamMember>()
            .eq(TeamMember::getUserId, captainId)
            .eq(TeamMember::getStatus, 1));
        if (count > 0) throw new BusinessException("您已加入其他团队");

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
        m.setJoinTime(new Date());
        teamMemberMapper.insert(m);

        return team;
    }

    @Override
    @Transactional
    public void joinTeam(String inviteCode, Long userId) {
        Team team = this.getOne(new LambdaQueryWrapper<Team>().eq(Team::getInviteCode, inviteCode));
        if (team == null) throw new BusinessException("邀请码无效");
        if (team.getStatus() != 0) throw new BusinessException("团队已锁定");
        if (team.getCaptainId().equals(userId)) throw new BusinessException("您是队长");

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
        m.setStatus(1);  // 直接通过，简化流程
        m.setJoinTime(new Date());
        teamMemberMapper.insert(m);

        // 通知队长
        notificationService.asyncNotify(team.getCaptainId(), "新成员加入了您的团队", "TEAM");
    }

    @Override
    public Map<String, Object> getMyTeam(Long userId) {
        TeamMember m = teamMemberMapper.selectOne(new LambdaQueryWrapper<TeamMember>()
            .eq(TeamMember::getUserId, userId)
            .eq(TeamMember::getStatus, 1));
        if (m == null) return Collections.emptyMap();
        Team team = this.getById(m.getTeamId());
        List<TeamMember> members = teamMemberMapper.selectList(new LambdaQueryWrapper<TeamMember>()
            .eq(TeamMember::getTeamId, team.getId()));
        Map<String, Object> map = new HashMap<>();
        map.put("team", team);
        map.put("members", members);
        map.put("myRole", m.getRole());
        return map;
    }

    @Override
    public void reviewMember(Long memberId, Boolean pass) {
        TeamMember m = teamMemberMapper.selectById(memberId);
        if (m == null) throw new BusinessException("记录不存在");
        m.setStatus(pass ? 1 : 2);
        teamMemberMapper.updateById(m);
    }

    @Override
    @Transactional
    public void kickMember(Long teamId, Long userId, Long operatorId) {
        Team team = this.getById(teamId);
        if (team == null) throw new BusinessException("团队不存在");
        if (!team.getCaptainId().equals(operatorId)) throw new BusinessException("仅队长可踢出成员");
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

        // 旧队长 -> 队员
        teamMemberMapper.update(new TeamMember(),
            new com.baomidou.mybatisplus.core.update.UpdateWrapper<TeamMember>()
                .eq("team_id", teamId).eq("user_id", oldCaptainId)
                .set("role", 0));
        // 新队长 -> 队长
        teamMemberMapper.update(new TeamMember(),
            new com.baomidou.mybatisplus.core.update.UpdateWrapper<TeamMember>()
                .eq("team_id", teamId).eq("user_id", newCaptainId)
                .set("role", 1));

        team.setCaptainId(newCaptainId);
        this.updateById(team);
        notificationService.asyncNotify(newCaptainId, "您已成为团队 " + team.getTeamName() + " 的队长", "TEAM");
    }

    @Override
    @Transactional
    public void submitTeamRegistration(Long teamId, String description, String attachment) {
        Team team = this.getById(teamId);
        if (team == null) throw new BusinessException("团队不存在");
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
        reg.setRegisterTime(new Date());
        teamRegistrationMapper.insert(reg);

        team.setStatus(1);
        this.updateById(team);
    }

    private String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
}

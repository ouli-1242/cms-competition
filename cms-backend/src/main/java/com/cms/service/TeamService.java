package com.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cms.dto.TeamDTO;
import com.cms.entity.Team;

import java.util.Map;

public interface TeamService extends IService<Team> {
    Team createTeam(TeamDTO dto, Long captainId);
    void joinTeam(String inviteCode, Long userId);
    Map<String, Object> getMyTeam(Long userId);
    void reviewMember(Long memberId, Boolean pass);
    void kickMember(Long teamId, Long userId, Long operatorId);
    void transferCaptain(Long teamId, Long newCaptainId, Long oldCaptainId);
    void submitTeamRegistration(Long teamId, String description, String attachment);
}

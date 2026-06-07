package com.cms.controller.student;

import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.dto.TeamDTO;
import com.cms.entity.Team;
import com.cms.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/student/team")
@PreAuthorize("hasRole('STUDENT')")
public class StudentTeamController {

    @Autowired private TeamService teamService;

    /** 创建团队 */
    @PostMapping
    public Result<Team> create(@RequestBody TeamDTO dto) {
        return Result.success(teamService.createTeam(dto, SecurityUtil.currentUserId()));
    }

    /** 加入团队（通过邀请码） */
    @PostMapping("/join")
    public Result join(@RequestParam String inviteCode) {
        teamService.joinTeam(inviteCode, SecurityUtil.currentUserId());
        return Result.success();
    }

    /** 我的团队 */
    @GetMapping("/my")
    public Result<Map<String, Object>> myTeam() {
        return Result.success(teamService.getMyTeam(SecurityUtil.currentUserId()));
    }

    /** 审核成员 */
    @PutMapping("/review")
    public Result review(@RequestParam Long memberId, @RequestParam Boolean pass) {
        teamService.reviewMember(memberId, pass);
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
        teamService.submitTeamRegistration(teamId, description, attachment);
        return Result.success();
    }
}

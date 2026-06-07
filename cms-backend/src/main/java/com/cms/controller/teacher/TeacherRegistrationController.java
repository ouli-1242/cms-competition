package com.cms.controller.teacher;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.common.exception.BusinessException;
import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.entity.Competition;
import com.cms.entity.Registration;
import com.cms.entity.TeamRegistration;
import com.cms.mapper.CompetitionMapper;
import com.cms.mapper.RegistrationMapper;
import com.cms.mapper.TeamRegistrationMapper;
import com.cms.service.RegistrationService;
import com.cms.service.TeamRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherRegistrationController {

    @Autowired private RegistrationService registrationService;
    @Autowired private TeamRegistrationService teamRegistrationService;
    @Autowired private RegistrationMapper registrationMapper;
    @Autowired private TeamRegistrationMapper teamRegistrationMapper;
    @Autowired private CompetitionMapper competitionMapper;

    /** 我指导的竞赛 ID 列表 */
    private List<Long> myCompIds() {
        return competitionMapper.selectIdsByTeacher(SecurityUtil.currentUserId());
    }

    private void checkAuth(Long competitionId) {
        Competition c = competitionMapper.selectById(competitionId);
        if (c == null) throw new BusinessException("竞赛不存在");
        if (!c.getTeacherId().equals(SecurityUtil.currentUserId())) {
            throw new BusinessException(403, "您不是该竞赛的指导老师");
        }
    }

    /** 个人报名列表 */
    @GetMapping("/registration/page")
    public Result<Page<Registration>> registrationPage(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Long competitionId,
        @RequestParam(required = false) Integer status,
        @RequestParam(required = false) String studentName) {
        Page<Registration> page = new Page<>(pageNum, pageSize);
        List<Long> ids = myCompIds();
        if (ids.isEmpty()) return Result.success(page);

        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        if (competitionId != null) wrapper.eq(Registration::getCompetitionId, competitionId);
        else wrapper.in(Registration::getCompetitionId, ids);
        if (status != null) wrapper.eq(Registration::getStatus, status);
        if (studentName != null && !studentName.isEmpty()) {
            wrapper.inSql(Registration::getUserId,
                "SELECT id FROM user WHERE real_name LIKE '%" + studentName + "%'");
        }
        wrapper.orderByDesc(Registration::getRegisterTime);
        return Result.success(registrationMapper.selectPage(page, wrapper));
    }

    /** 个人报名审核 */
    @PutMapping("/registration/{id}/review")
    public Result reviewRegistration(@PathVariable Long id,
                                      @RequestParam Boolean pass,
                                      @RequestParam(required = false) String remark) {
        Registration r = registrationMapper.selectById(id);
        if (r == null) return Result.error("记录不存在");
        checkAuth(r.getCompetitionId());
        registrationService.review(id, pass, remark, SecurityUtil.currentUserId());
        return Result.success();
    }

    /** 团队报名列表 */
    @GetMapping("/team-registration/page")
    public Result<Page<TeamRegistration>> teamRegPage(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "10") Integer pageSize,
        @RequestParam(required = false) Long competitionId,
        @RequestParam(required = false) Integer status) {
        Page<TeamRegistration> page = new Page<>(pageNum, pageSize);
        List<Long> ids = myCompIds();
        if (ids.isEmpty()) return Result.success(page);

        LambdaQueryWrapper<TeamRegistration> wrapper = new LambdaQueryWrapper<>();
        if (competitionId != null) wrapper.eq(TeamRegistration::getCompetitionId, competitionId);
        else wrapper.in(TeamRegistration::getCompetitionId, ids);
        if (status != null) wrapper.eq(TeamRegistration::getStatus, status);
        wrapper.orderByDesc(TeamRegistration::getRegisterTime);
        return Result.success(teamRegistrationMapper.selectPage(page, wrapper));
    }

    /** 团队报名审核 */
    @PutMapping("/team-registration/{id}/review")
    public Result reviewTeamReg(@PathVariable Long id,
                                 @RequestParam Boolean pass,
                                 @RequestParam(required = false) String remark) {
        TeamRegistration r = teamRegistrationMapper.selectById(id);
        if (r == null) return Result.error("记录不存在");
        checkAuth(r.getCompetitionId());
        teamRegistrationService.review(id, pass, remark, SecurityUtil.currentUserId());
        return Result.success();
    }
}

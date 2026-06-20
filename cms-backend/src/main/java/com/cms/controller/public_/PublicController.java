package com.cms.controller.public_;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.common.result.Result;
import com.cms.entity.Banner;
import com.cms.entity.Competition;
import com.cms.entity.HotRecommend;
import com.cms.entity.Registration;
import com.cms.entity.TeamRegistration;
import com.cms.mapper.BannerMapper;
import com.cms.mapper.CompetitionMapper;
import com.cms.mapper.RegistrationMapper;
import com.cms.mapper.TeamRegistrationMapper;
import com.cms.service.BannerService;
import com.cms.service.HotRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PublicController {

    @Autowired private CompetitionMapper competitionMapper;
    @Autowired private BannerService bannerService;
    @Autowired private HotRecommendService hotRecommendService;
    @Autowired private RegistrationMapper registrationMapper;
    @Autowired private TeamRegistrationMapper teamRegistrationMapper;

    /** 首页轮播图 */
    @GetMapping("/banner/public/list")
    public Result<List<Banner>> banners() {
        return Result.success(bannerService.listActive());
    }

    /** 首页热门推荐 */
    @GetMapping("/hot-recommend/public/list")
    public Result<List<Competition>> hotList() {
        return Result.success(hotRecommendService.listRecommend());
    }

    /** 公开竞赛列表 */
    @GetMapping("/competition/public/page")
    public Result<Page<Competition>> competitionPage(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "12") Integer pageSize,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Integer type,
        @RequestParam(required = false) Integer registrationStatus,
        @RequestParam(required = false) Boolean excludeEnded) {
        Page<Competition> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getStatus, 1);
        if (keyword  != null && !keyword.isEmpty())  wrapper.like(Competition::getTitle,    keyword);
        if (category != null && !category.isEmpty()) wrapper.eq (Competition::getCategory, category);
        if (type     != null) wrapper.eq(Competition::getType, type);

        // 按报名状态（日期）服务端过滤
        LocalDateTime now = LocalDateTime.now();
        if (Boolean.TRUE.equals(excludeEnded)) {
            wrapper.ge(Competition::getRegisterEnd, now);
        }
        if (registrationStatus != null) {
            if (registrationStatus == 1) {
                // 报名中：registerStart <= now <= registerEnd
                wrapper.le(Competition::getRegisterStart, now).ge(Competition::getRegisterEnd, now);
            } else if (registrationStatus == 0) {
                // 即将开始：registerStart > now
                wrapper.gt(Competition::getRegisterStart, now);
            } else if (registrationStatus == 2) {
                // 已截止：registerEnd < now
                wrapper.lt(Competition::getRegisterEnd, now);
            }
        }

        // 排序：报名中 > 即将开始 > 已截止，同组内按报名截止时间升序
        wrapper.last("ORDER BY CASE " +
            "WHEN register_start <= NOW() AND register_end >= NOW() THEN 0 " +
            "WHEN register_start > NOW() THEN 1 " +
            "ELSE 2 END, register_end ASC");
        Page<Competition> result = competitionMapper.selectPage(page, wrapper);

        // 填充报名人数/团队数
        for (Competition c : result.getRecords()) {
            if (c.getType() != null && c.getType() == 2) {
                // 团队赛：统计团队数
                Long teamCount = teamRegistrationMapper.selectCount(
                    new LambdaQueryWrapper<TeamRegistration>()
                        .eq(TeamRegistration::getCompetitionId, c.getId())
                        .eq(TeamRegistration::getStatus, 1)
                );
                c.setRegistrationCount(teamCount.intValue());
            } else {
                // 个人赛：统计人数
                Long regCount = registrationMapper.selectCount(
                    new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getCompetitionId, c.getId())
                        .eq(Registration::getStatus, 1)
                );
                c.setRegistrationCount(regCount.intValue());
            }
        }

        return Result.success(result);
    }

    /** 公开竞赛详情 */
    @GetMapping("/competition/public/{id}")
    public Result<Competition> competitionDetail(@PathVariable Long id) {
        Competition c = competitionMapper.selectById(id);
        if (c == null || c.getStatus() != 1 || c.getIsDeleted() != 0) {
            return Result.error(404, "竞赛不存在或未发布");
        }
        if (c.getType() != null && c.getType() == 2) {
            Long teamCount = teamRegistrationMapper.selectCount(
                new LambdaQueryWrapper<TeamRegistration>()
                    .eq(TeamRegistration::getCompetitionId, c.getId())
                    .eq(TeamRegistration::getStatus, 1)
            );
            c.setRegistrationCount(teamCount.intValue());
        } else {
            Long regCount = registrationMapper.selectCount(
                new LambdaQueryWrapper<Registration>()
                    .eq(Registration::getCompetitionId, c.getId())
                    .eq(Registration::getStatus, 1)
            );
            c.setRegistrationCount(regCount.intValue());
        }
        return Result.success(c);
    }
}

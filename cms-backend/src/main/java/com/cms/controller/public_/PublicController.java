package com.cms.controller.public_;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.common.result.Result;
import com.cms.entity.Banner;
import com.cms.entity.Competition;
import com.cms.entity.HotRecommend;
import com.cms.mapper.BannerMapper;
import com.cms.mapper.CompetitionMapper;
import com.cms.service.BannerService;
import com.cms.service.HotRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PublicController {

    @Autowired private CompetitionMapper competitionMapper;
    @Autowired private BannerService bannerService;
    @Autowired private HotRecommendService hotRecommendService;

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
        @RequestParam(required = false) Integer type) {
        Page<Competition> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Competition> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Competition::getStatus, 1).eq(Competition::getIsDeleted, 0);
        if (keyword  != null && !keyword.isEmpty())  wrapper.like(Competition::getTitle,    keyword);
        if (category != null && !category.isEmpty()) wrapper.eq (Competition::getCategory, category);
        if (type     != null) wrapper.eq(Competition::getType, type);
        wrapper.orderByDesc(Competition::getCreateTime);
        return Result.success(competitionMapper.selectPage(page, wrapper));
    }

    /** 公开竞赛详情 */
    @GetMapping("/competition/public/{id}")
    public Result<Competition> competitionDetail(@PathVariable Long id) {
        return Result.success(competitionMapper.selectById(id));
    }
}

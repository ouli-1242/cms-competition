package com.cms.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.common.result.Result;
import com.cms.dto.BannerDTO;
import com.cms.entity.Banner;
import com.cms.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/banner")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBannerController {

    @Autowired private BannerService bannerService;

    @GetMapping("/page")
    public Result<Page<Banner>> page(
        @RequestParam(defaultValue = "1") Integer pageNum,
        @RequestParam(defaultValue = "20") Integer pageSize) {
        return Result.success(bannerService.page(new Page<>(pageNum, pageSize)));
    }

    @PostMapping
    public Result<Banner> add(@RequestBody BannerDTO dto) {
        return Result.success(bannerService.add(dto));
    }

    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody BannerDTO dto) {
        bannerService.update(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        bannerService.delete(id);
        return Result.success();
    }
}

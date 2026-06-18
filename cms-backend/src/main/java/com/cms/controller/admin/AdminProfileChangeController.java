package com.cms.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.entity.ProfileChangeRequest;
import com.cms.entity.User;
import com.cms.service.ProfileChangeRequestService;
import com.cms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/profile-change")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProfileChangeController {

    @Autowired
    private ProfileChangeRequestService profileChangeRequestService;
    @Autowired
    private UserService userService;

    /** 修改申请分页列表 */
    @GetMapping("/page")
    public Result<Page<ProfileChangeRequest>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) Integer status) {

        Page<ProfileChangeRequest> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ProfileChangeRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ProfileChangeRequest::getStatus, status);
        }
        wrapper.orderByDesc(ProfileChangeRequest::getCreateTime);
        Page<ProfileChangeRequest> result = profileChangeRequestService.page(page, wrapper);

        // 关联查询用户信息
        result.getRecords().forEach(req -> {
            User u = userService.getById(req.getUserId());
            if (u != null) {
                req.setUserRealName(u.getRealName());
                req.setUserUsername(u.getUsername());
            }
        });

        return Result.success(result);
    }

    /** 审批 */
    @PutMapping("/{id}/review")
    public Result review(@PathVariable Long id, @RequestParam boolean pass,
                         @RequestParam(required = false) String remark) {
        profileChangeRequestService.reviewRequest(id, pass, remark, SecurityUtil.currentUserId());
        return Result.success();
    }
}

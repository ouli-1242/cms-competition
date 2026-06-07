package com.cms.controller.student;

import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.dto.ProfileDTO;
import com.cms.entity.User;
import com.cms.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/profile")
@PreAuthorize("hasRole('STUDENT')")
public class StudentProfileController {

    @Autowired private UserService userService;

    @GetMapping
    public Result<User> get() {
        return Result.success(userService.getProfile(SecurityUtil.currentUserId()));
    }

    @PutMapping
    public Result update(@RequestBody ProfileDTO dto) {
        User u = new User();
        BeanUtils.copyProperties(dto, u);
        userService.updateProfile(SecurityUtil.currentUserId(), u);
        return Result.success();
    }

    @PutMapping("/password")
    public Result changePassword(@RequestParam String oldPwd, @RequestParam String newPwd) {
        userService.changePassword(SecurityUtil.currentUserId(), oldPwd, newPwd);
        return Result.success();
    }
}

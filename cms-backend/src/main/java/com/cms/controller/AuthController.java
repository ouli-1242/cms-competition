package com.cms.controller;

import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.dto.LoginDTO;
import com.cms.dto.RegisterDTO;
import com.cms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @GetMapping("/info")
    public Result<?> info() {
        return Result.success(userService.getProfile(SecurityUtil.currentUserId()));
    }

    @PostMapping("/logout")
    public Result logout() {
        return Result.success();
    }
}

package com.cms.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cms.common.exception.BusinessException;
import com.cms.common.result.Result;
import com.cms.entity.User;
import com.cms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 创建教师账号 */
    @PostMapping
    public Result<?> createTeacher(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.trim().isEmpty()) throw new BusinessException("账号不能为空");
        Long exists = userService.count(new LambdaQueryWrapper<User>().eq(User::getUsername, username.trim()));
        if (exists > 0) throw new BusinessException("该账号已存在");

        User u = new User();
        u.setUsername(username.trim());
        u.setPassword(passwordEncoder.encode("123456"));
        u.setRealName(body.get("realName"));
        u.setCollege(body.get("college"));
        u.setPhone(body.get("phone"));
        u.setEmail(body.get("email"));
        u.setRole("TEACHER");
        userService.save(u);
        return Result.success();
    }

    /** 用户分页列表 */
    @GetMapping("/page")
    public Result<Page<User>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {

        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w
                .like(User::getRealName, kw)
                .or().like(User::getUsername, kw)
                .or().like(User::getPhone, kw)
            );
        }
        if (role != null && !role.trim().isEmpty()) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreateTime);
        return Result.success(userService.page(page, wrapper));
    }
}

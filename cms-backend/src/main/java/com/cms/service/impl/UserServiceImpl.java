package com.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cms.common.exception.BusinessException;
import com.cms.common.util.JwtUtil;
import com.cms.dto.LoginDTO;
import com.cms.dto.RegisterDTO;
import com.cms.entity.User;
import com.cms.mapper.UserMapper;
import com.cms.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        User u = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (u == null) throw new BusinessException("用户不存在");
        if (u.getIsDeleted() != null && u.getIsDeleted() == 1) throw new BusinessException("账号已禁用");
        if (!passwordEncoder.matches(dto.getPassword(), u.getPassword())) {
            throw new BusinessException("密码错误");
        }
        String token = jwtUtil.generate(u.getId(), u.getRole());
        u.setPassword(null);
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("user", u);
        return map;
    }

    @Override
    public void register(RegisterDTO dto) {
        if (this.count(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername())) > 0) {
            throw new BusinessException("用户名已存在");
        }
        User u = new User();
        u.setUsername(dto.getUsername());
        u.setPassword(passwordEncoder.encode(dto.getPassword()));
        u.setNickname(dto.getNickname());
        u.setRealName(dto.getRealName());
        u.setSchool(dto.getSchool());
        u.setPhone(dto.getPhone());
        u.setRole("STUDENT");
        this.save(u);
    }

    @Override
    public User getProfile(Long userId) {
        User u = this.getById(userId);
        if (u != null) u.setPassword(null);
        return u;
    }

    @Override
    public void updateProfile(Long userId, User user) {
        User u = new User();
        u.setId(userId);
        u.setNickname(user.getNickname());
        u.setRealName(user.getRealName());
        u.setSchool(user.getSchool());
        u.setPhone(user.getPhone());
        u.setEmail(user.getEmail());
        u.setAvatar(user.getAvatar());
        this.updateById(u);
    }

    @Override
    public void changePassword(Long userId, String oldPwd, String newPwd) {
        User u = this.getById(userId);
        if (u == null) throw new BusinessException("用户不存在");
        if (!passwordEncoder.matches(oldPwd, u.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        u.setPassword(passwordEncoder.encode(newPwd));
        this.updateById(u);
    }
}

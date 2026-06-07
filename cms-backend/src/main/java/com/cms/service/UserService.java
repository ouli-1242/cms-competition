package com.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cms.dto.LoginDTO;
import com.cms.dto.RegisterDTO;
import com.cms.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> login(LoginDTO dto);
    void register(RegisterDTO dto);
    User getProfile(Long userId);
    void updateProfile(Long userId, User user);
    void changePassword(Long userId, String oldPwd, String newPwd);
}

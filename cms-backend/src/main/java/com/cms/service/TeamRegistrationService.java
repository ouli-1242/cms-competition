package com.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cms.entity.TeamRegistration;

public interface TeamRegistrationService extends IService<TeamRegistration> {
    void review(Long id, Boolean pass, String remark, Long reviewerId);
}

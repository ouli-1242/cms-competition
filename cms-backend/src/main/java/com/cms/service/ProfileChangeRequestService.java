package com.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cms.entity.ProfileChangeRequest;

public interface ProfileChangeRequestService extends IService<ProfileChangeRequest> {
    ProfileChangeRequest submitRequest(Long userId, String fieldName, String newValue);
    java.util.List<ProfileChangeRequest> getMyRequests(Long userId);
    void reviewRequest(Long requestId, boolean approved, String remark, Long reviewerId);
}

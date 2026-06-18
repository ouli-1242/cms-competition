package com.cms.controller.student;

import com.cms.common.result.Result;
import com.cms.common.util.SecurityUtil;
import com.cms.entity.ProfileChangeRequest;
import com.cms.service.ProfileChangeRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student/profile-change")
@PreAuthorize("hasRole('STUDENT')")
public class StudentProfileChangeController {

    @Autowired
    private ProfileChangeRequestService profileChangeRequestService;

    /** 提交修改申请 */
    @PostMapping
    public Result submit(@RequestBody Map<String, String> body) {
        String fieldName = body.get("fieldName");
        String newValue = body.get("newValue");

        if (fieldName == null || newValue == null || newValue.trim().isEmpty()) {
            return Result.error("参数不完整");
        }
        if (!"username".equals(fieldName) && !"email".equals(fieldName)) {
            return Result.error("不支持修改该字段");
        }

        ProfileChangeRequest req = profileChangeRequestService.submitRequest(
                SecurityUtil.currentUserId(), fieldName, newValue.trim());
        if (req == null) {
            return Result.error("新值与当前值相同，无需修改");
        }
        return Result.success(req);
    }

    /** 我的修改申请记录 */
    @GetMapping("/my")
    public Result<List<ProfileChangeRequest>> myRequests() {
        return Result.success(profileChangeRequestService.getMyRequests(SecurityUtil.currentUserId()));
    }
}

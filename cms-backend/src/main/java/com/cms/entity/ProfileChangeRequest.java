package com.cms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 个人资料修改申请
 */
@Data
@TableName("profile_change_request")
public class ProfileChangeRequest {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String fieldName;   // username / email
    private String oldValue;
    private String newValue;
    private Integer status;     // 0=pending, 1=approved, 2=rejected
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private LocalDateTime reviewTime;
    private Long reviewerId;
    private String reviewRemark;
    @TableLogic
    private Integer isDeleted;

    /** 关联用户名 */
    @TableField(exist = false)
    private String userRealName;
    @TableField(exist = false)
    private String userUsername;
}

package com.cms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 个人报名
 */
@Data
@TableName("registration")
public class Registration {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long competitionId;
    private Long userId;
    private Integer status;            // 0待审核 1已通过 2已拒绝
    private String attachment;
    private String description;
    private LocalDateTime registerTime;
    private Long reviewerId;
    private String reviewRemark;
    private LocalDateTime reviewTime;
    @TableLogic
    private Integer isDeleted;
}

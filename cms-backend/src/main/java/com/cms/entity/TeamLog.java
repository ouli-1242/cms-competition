package com.cms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 团队操作日志
 */
@Data
@TableName("team_log")
public class TeamLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teamId;
    private Long operatorId;
    private String action;
    private String detail;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

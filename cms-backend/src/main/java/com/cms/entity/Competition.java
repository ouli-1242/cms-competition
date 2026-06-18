package com.cms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 竞赛
 */
@Data
@TableName("competition")
public class Competition {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String description;
    private String category;
    private Integer type;              // 1个人 2团队
    private Integer maxTeamSize;
    private Integer minTeamSize;
    private LocalDateTime registerStart;
    private LocalDateTime registerEnd;
    private LocalDateTime compStart;
    private LocalDateTime compEnd;
    private String cover;
    private String attachment;
    private Long publisherId;
    private Long teacherId;
    private Integer status;            // 0下架 1上架
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer isDeleted;

    /** 报名人数/团队数（非数据库字段） */
    @TableField(exist = false)
    private Integer registrationCount;
}

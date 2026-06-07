package com.cms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 团队成员
 */
@Data
@TableName("team_member")
public class TeamMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teamId;
    private Long userId;
    private Integer role;              // 0队员 1队长
    private Integer status;            // 0待审核 1已加入 2已拒绝
    private LocalDateTime joinTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

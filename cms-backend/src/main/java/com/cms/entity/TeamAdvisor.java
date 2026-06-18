package com.cms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 团队指导老师邀请
 */
@Data
@TableName("team_advisor")
public class TeamAdvisor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long teamId;
    private Long teacherId;
    private Integer status;            // 0待审核 1已接受 2已拒绝
    private Long invitedBy;            // 邀请人(队长)userId
    private LocalDateTime inviteTime;
    private LocalDateTime acceptTime;
    private LocalDateTime rejectTime;
    private String remark;
}

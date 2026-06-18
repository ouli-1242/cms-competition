package com.cms.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 指导老师邀请 VO（含enriched展示字段）
 */
@Data
public class AdvisorInvitationVO {
    private Long id;
    private Long teamId;
    private Long teacherId;
    private Integer status;
    private Long invitedBy;
    private LocalDateTime inviteTime;
    private LocalDateTime acceptTime;
    private LocalDateTime rejectTime;
    private String remark;

    // enriched
    private String teacherName;
    private String teacherCollege;
    private String teamName;
    private String competitionName;
    private String inviterName;
}

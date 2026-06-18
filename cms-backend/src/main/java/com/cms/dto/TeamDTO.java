package com.cms.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class TeamDTO {
    @NotBlank private String teamName;
    private String inviteCode;  // 可选 - 加入时用
    private Long competitionId;          // 创建时必填
    private Integer maxSize;
    private String slogan;
    private List<Long> studentIds;  // 创建时邀请的学生userId列表
    private Long teacherId;         // 创建时邀请的指导老师userId
}

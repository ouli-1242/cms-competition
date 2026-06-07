package com.cms.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class TeamDTO {
    @NotBlank private String teamName;
    @NotBlank private String inviteCode;  // 可选 - 加入时用
    private Long competitionId;          // 创建时必填
    private Integer maxSize;
    private String slogan;
}

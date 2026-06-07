package com.cms.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class CompetitionDTO {
    @NotBlank private String title;
    private String description;
    private String category;
    private Integer type;             // 1个人 2团队
    private Integer maxTeamSize;
    private Integer minTeamSize;
    private LocalDateTime registerStart;
    private LocalDateTime registerEnd;
    private LocalDateTime compStart;
    private LocalDateTime compEnd;
    private String cover;
    private String attachment;
    private Long teacherId;
}

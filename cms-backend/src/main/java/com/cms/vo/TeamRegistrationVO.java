package com.cms.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeamRegistrationVO {
    private Long id;
    private Long competitionId;
    private Long teamId;
    private Integer status;
    private String attachment;
    private String description;
    private LocalDateTime registerTime;
    private Long reviewerId;
    private String reviewRemark;
    private LocalDateTime reviewTime;

    private String teamName;
    private String captainName;
    private Integer memberCount;
    private Integer maxSize;
    private String competitionName;
}

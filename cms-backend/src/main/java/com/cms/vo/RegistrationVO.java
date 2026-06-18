package com.cms.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RegistrationVO {
    private Long id;
    private Long competitionId;
    private Long userId;
    private Integer status;
    private String attachment;
    private String description;
    private LocalDateTime registerTime;
    private Long reviewerId;
    private String reviewRemark;
    private LocalDateTime reviewTime;

    private String studentName;
    private String college;
    private String competitionName;
}

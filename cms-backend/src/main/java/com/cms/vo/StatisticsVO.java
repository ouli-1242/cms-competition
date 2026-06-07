package com.cms.vo;

import com.cms.entity.Competition;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StatisticsVO {
    private Long competitionCount;
    private Integer totalCount;
    private Integer individualCount;
    private Integer teamCount;
    private Integer passedCount;
    private Double passRate;
}

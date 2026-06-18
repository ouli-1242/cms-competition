package com.cms.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class BannerDTO {
    private String title;
    @NotBlank private String imageUrl;
    private String linkUrl;
    private Integer sort;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

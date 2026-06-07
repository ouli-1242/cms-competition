package com.cms.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class BannerDTO {
    private String title;
    @NotBlank private String imageUrl;
    private String linkUrl;
    private Integer sort;
    private Integer status;
    private String startTime;
    private String endTime;
}

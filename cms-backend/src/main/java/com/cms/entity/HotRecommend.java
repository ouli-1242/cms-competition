package com.cms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 热门推荐
 */
@Data
@TableName("hot_recommend")
public class HotRecommend {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long competitionId;
    private Integer sort;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

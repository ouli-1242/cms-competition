package com.cms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cms.entity.Competition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CompetitionMapper extends BaseMapper<Competition> {

    @Select("SELECT id FROM competition WHERE teacher_id = #{teacherId} AND is_deleted = 0")
    List<Long> selectIdsByTeacher(Long teacherId);
}

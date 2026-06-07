package com.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cms.dto.CompetitionDTO;
import com.cms.entity.Competition;

import java.util.List;

public interface CompetitionService extends IService<Competition> {
    Competition publish(CompetitionDTO dto, Long publisherId);
    void updateCompetition(Long id, CompetitionDTO dto);
    void changeStatus(Long id, Integer status);
    void deleteCompetition(Long id);
    List<Long> listIdsByTeacher(Long teacherId);
}

package com.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cms.entity.Registration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface RegistrationService extends IService<Registration> {

    @Transactional
    Registration registerIndividual(Long competitionId, Long userId, String description, String attachment);

    void cancelRegistration(Long id, Long userId);

    void review(Long id, Boolean pass, String remark, Long reviewerId);

    List<Registration> listForExport(Long competitionId, Integer status, String studentName, Long teacherId);
}

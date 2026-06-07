package com.cms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cms.dto.BannerDTO;
import com.cms.entity.Banner;

import java.util.List;

public interface BannerService extends IService<Banner> {
    Banner add(BannerDTO dto);
    void update(Long id, BannerDTO dto);
    void delete(Long id);
    List<Banner> listActive();
}

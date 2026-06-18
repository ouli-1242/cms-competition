package com.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cms.dto.BannerDTO;
import com.cms.entity.Banner;
import com.cms.mapper.BannerMapper;
import com.cms.service.BannerService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    public Banner add(BannerDTO dto) {
        Banner b = new Banner();
        BeanUtils.copyProperties(dto, b);
        this.save(b);
        return b;
    }

    @Override
    public void update(Long id, BannerDTO dto) {
        Banner b = new Banner();
        BeanUtils.copyProperties(dto, b);
        b.setId(id);
        this.updateById(b);
    }

    @Override
    public void delete(Long id) {
        this.removeById(id);
    }

    @Override
    public List<Banner> listActive() {
        LocalDateTime now = LocalDateTime.now();
        return this.list(new LambdaQueryWrapper<Banner>()
            .eq(Banner::getStatus, 1)
            .and(w -> w.isNull(Banner::getStartTime).or().le(Banner::getStartTime, now))
            .and(w -> w.isNull(Banner::getEndTime).or().ge(Banner::getEndTime, now))
            .orderByAsc(Banner::getSort));
    }
}

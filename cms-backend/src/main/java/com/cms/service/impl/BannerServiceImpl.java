package com.cms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cms.dto.BannerDTO;
import com.cms.entity.Banner;
import com.cms.mapper.BannerMapper;
import com.cms.service.BannerService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Banner add(BannerDTO dto) {
        Banner b = new Banner();
        BeanUtils.copyProperties(dto, b);
        this.save(b);
        redisTemplate.delete("home:banners");
        return b;
    }

    @Override
    public void update(Long id, BannerDTO dto) {
        Banner b = new Banner();
        BeanUtils.copyProperties(dto, b);
        b.setId(id);
        this.updateById(b);
        redisTemplate.delete("home:banners");
    }

    @Override
    public void delete(Long id) {
        this.removeById(id);
        redisTemplate.delete("home:banners");
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Banner> listActive() {
        // 1. 查 Redis
        Object cached = redisTemplate.opsForValue().get("home:banners");
        if (cached instanceof List) {
            return (List<Banner>) cached;
        }
        // 2. 查 DB
        LocalDateTime now = LocalDateTime.now();
        List<Banner> list = this.list(new LambdaQueryWrapper<Banner>()
            .eq(Banner::getStatus, 1)
            .and(w -> w.isNull(Banner::getStartTime).or().le(Banner::getStartTime, now))
            .and(w -> w.isNull(Banner::getEndTime).or().ge(Banner::getEndTime, now))
            .orderByAsc(Banner::getSort));
        // 3. 缓存 5 分钟
        redisTemplate.opsForValue().set("home:banners", list, 5, java.util.concurrent.TimeUnit.MINUTES);
        return list;
    }
}

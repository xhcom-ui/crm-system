package com.crm.marketing.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.marketing.entity.Campaign;
import com.crm.marketing.mapper.CampaignMapper;
import com.crm.marketing.service.CampaignService;
import cn.hutool.core.util.StrUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;

@Service
public class CampaignServiceImpl extends ServiceImpl<CampaignMapper, Campaign> implements CampaignService {

    @Override
    @Cacheable(value = "campaign", key = "'page:' + (#p1 ?: '') + ':' + #p0.current + ':' + #p0.size", unless = "#result == null || #result.records.size() == 0")
    public IPage<Campaign> pageCampaigns(Page<Campaign> page, String keyword) {
        LambdaQueryWrapper<Campaign> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Campaign::getName, keyword);
        }
        wrapper.orderByDesc(Campaign::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @CacheEvict(value = "campaign", allEntries = true)
    public boolean save(Campaign entity) {
        return super.save(entity);
    }

    @Override
    @CacheEvict(value = "campaign", allEntries = true)
    public boolean updateById(Campaign entity) {
        return super.updateById(entity);
    }

    @Override
    @CacheEvict(value = "campaign", allEntries = true)
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    @Override
    @CacheEvict(value = "campaign", allEntries = true)
    public boolean removeByIds(Collection<?> list) {
        return super.removeByIds(list);
    }
}

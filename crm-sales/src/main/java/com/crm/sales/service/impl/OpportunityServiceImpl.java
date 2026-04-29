package com.crm.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.sales.entity.Opportunity;
import com.crm.sales.mapper.OpportunityMapper;
import com.crm.sales.service.OpportunityService;
import cn.hutool.core.util.StrUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;

@Service
public class OpportunityServiceImpl extends ServiceImpl<OpportunityMapper, Opportunity> implements OpportunityService {

    @Override
    @Cacheable(value = "opportunity", key = "'page:' + (#p1 ?: '') + ':' + #p0.current + ':' + #p0.size", unless = "#result == null || #result.records.size() == 0")
    public IPage<Opportunity> pageOpportunities(Page<Opportunity> page, String keyword) {
        LambdaQueryWrapper<Opportunity> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Opportunity::getName, keyword);
        }
        wrapper.orderByDesc(Opportunity::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @CacheEvict(value = "opportunity", allEntries = true)
    public boolean save(Opportunity entity) {
        return super.save(entity);
    }

    @Override
    @CacheEvict(value = "opportunity", allEntries = true)
    public boolean updateById(Opportunity entity) {
        return super.updateById(entity);
    }

    @Override
    @CacheEvict(value = "opportunity", allEntries = true)
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    @Override
    @CacheEvict(value = "opportunity", allEntries = true)
    public boolean removeByIds(Collection<?> list) {
        return super.removeByIds(list);
    }
}

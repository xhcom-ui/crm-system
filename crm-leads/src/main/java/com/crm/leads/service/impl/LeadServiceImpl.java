package com.crm.leads.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.leads.entity.Lead;
import com.crm.leads.mapper.LeadMapper;
import com.crm.leads.service.LeadService;
import cn.hutool.core.util.StrUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;

@Service
public class LeadServiceImpl extends ServiceImpl<LeadMapper, Lead> implements LeadService {

    @Override
    @Cacheable(value = "lead", key = "'page:' + (#p1 ?: '') + ':' + #p0.current + ':' + #p0.size", unless = "#result == null || #result.records.size() == 0")
    public IPage<Lead> pageLeads(Page<Lead> page, String keyword) {
        LambdaQueryWrapper<Lead> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Lead::getName, keyword).or().like(Lead::getContactInfo, keyword);
        }
        wrapper.orderByDesc(Lead::getScore).orderByDesc(Lead::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @CacheEvict(value = "lead", allEntries = true)
    public boolean save(Lead entity) {
        return super.save(entity);
    }

    @Override
    @CacheEvict(value = "lead", allEntries = true)
    public boolean updateById(Lead entity) {
        return super.updateById(entity);
    }

    @Override
    @CacheEvict(value = "lead", allEntries = true)
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    @Override
    @CacheEvict(value = "lead", allEntries = true)
    public boolean removeByIds(Collection<?> list) {
        return super.removeByIds(list);
    }
}

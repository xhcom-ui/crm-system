package com.crm.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.workflow.entity.Workflow;
import com.crm.workflow.mapper.WorkflowMapper;
import com.crm.workflow.service.WorkflowService;
import cn.hutool.core.util.StrUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;

@Service
public class WorkflowServiceImpl extends ServiceImpl<WorkflowMapper, Workflow> implements WorkflowService {

    @Override
    @Cacheable(value = "workflow", key = "'page:' + (#p1 ?: '') + ':' + #p0.current + ':' + #p0.size", unless = "#result == null || #result.records.size() == 0")
    public IPage<Workflow> pageWorkflows(Page<Workflow> page, String keyword) {
        LambdaQueryWrapper<Workflow> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Workflow::getName, keyword);
        }
        wrapper.orderByDesc(Workflow::getPriority).orderByDesc(Workflow::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @CacheEvict(value = "workflow", allEntries = true)
    public boolean save(Workflow entity) {
        return super.save(entity);
    }

    @Override
    @CacheEvict(value = "workflow", allEntries = true)
    public boolean updateById(Workflow entity) {
        return super.updateById(entity);
    }

    @Override
    @CacheEvict(value = "workflow", allEntries = true)
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    @Override
    @CacheEvict(value = "workflow", allEntries = true)
    public boolean removeByIds(Collection<?> list) {
        return super.removeByIds(list);
    }
}

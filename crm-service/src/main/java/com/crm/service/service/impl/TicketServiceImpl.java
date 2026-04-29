package com.crm.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.service.entity.Ticket;
import com.crm.service.mapper.TicketMapper;
import com.crm.service.service.TicketService;
import cn.hutool.core.util.StrUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;

@Service
public class TicketServiceImpl extends ServiceImpl<TicketMapper, Ticket> implements TicketService {

    @Override
    @Cacheable(value = "ticket", key = "'page:' + (#p1 ?: '') + ':' + #p0.current + ':' + #p0.size", unless = "#result == null || #result.records.size() == 0")
    public IPage<Ticket> pageTickets(Page<Ticket> page, String keyword) {
        LambdaQueryWrapper<Ticket> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Ticket::getTitle, keyword).or().like(Ticket::getTicketNo, keyword);
        }
        wrapper.orderByDesc(Ticket::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @CacheEvict(value = "ticket", allEntries = true)
    public boolean save(Ticket entity) {
        return super.save(entity);
    }

    @Override
    @CacheEvict(value = "ticket", allEntries = true)
    public boolean updateById(Ticket entity) {
        return super.updateById(entity);
    }

    @Override
    @CacheEvict(value = "ticket", allEntries = true)
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    @Override
    @CacheEvict(value = "ticket", allEntries = true)
    public boolean removeByIds(Collection<?> list) {
        return super.removeByIds(list);
    }
}

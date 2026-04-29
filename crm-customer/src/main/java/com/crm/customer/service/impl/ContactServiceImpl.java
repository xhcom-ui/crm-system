package com.crm.customer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.customer.entity.Contact;
import com.crm.customer.mapper.ContactMapper;
import com.crm.customer.service.ContactService;
import cn.hutool.core.util.StrUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collection;

/**
 * 客户联系人 Service 实现
 */
@Service
public class ContactServiceImpl extends ServiceImpl<ContactMapper, Contact> implements ContactService {

    @Override
    @Cacheable(value = "contact", key = "'page:' + (#p1 ?: '') + ':' + #p0.current + ':' + #p0.size", unless = "#result == null || #result.records.size() == 0")
    public IPage<Contact> pageContacts(Page<Contact> page, String keyword) {
        LambdaQueryWrapper<Contact> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Contact::getName, keyword)
                    .or()
                    .like(Contact::getCompany, keyword)
                    .or()
                    .like(Contact::getPhone, keyword);
        }
        wrapper.orderByDesc(Contact::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    @CacheEvict(value = "contact", allEntries = true)
    public boolean save(Contact entity) {
        return super.save(entity);
    }

    @Override
    @CacheEvict(value = "contact", allEntries = true)
    public boolean updateById(Contact entity) {
        return super.updateById(entity);
    }

    @Override
    @CacheEvict(value = "contact", allEntries = true)
    public boolean removeById(Serializable id) {
        return super.removeById(id);
    }

    @Override
    @CacheEvict(value = "contact", allEntries = true)
    public boolean removeByIds(Collection<?> list) {
        return super.removeByIds(list);
    }
}

package com.crm.customer.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.customer.entity.Contact;

/**
 * 客户联系人 Service 接口
 */
public interface ContactService extends IService<Contact> {

    /**
     * 分页查询联系人
     */
    IPage<Contact> pageContacts(Page<Contact> page, String keyword);
}

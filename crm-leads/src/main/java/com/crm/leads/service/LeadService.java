package com.crm.leads.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.leads.entity.Lead;

public interface LeadService extends IService<Lead> {
    IPage<Lead> pageLeads(Page<Lead> page, String keyword);
}

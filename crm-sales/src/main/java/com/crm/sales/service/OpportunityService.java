package com.crm.sales.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.sales.entity.Opportunity;

public interface OpportunityService extends IService<Opportunity> {
    IPage<Opportunity> pageOpportunities(Page<Opportunity> page, String keyword);
}

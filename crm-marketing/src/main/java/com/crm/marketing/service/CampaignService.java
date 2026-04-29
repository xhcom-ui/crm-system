package com.crm.marketing.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.marketing.entity.Campaign;

public interface CampaignService extends IService<Campaign> {
    IPage<Campaign> pageCampaigns(Page<Campaign> page, String keyword);
}

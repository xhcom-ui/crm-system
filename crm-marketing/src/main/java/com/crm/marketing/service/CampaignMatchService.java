package com.crm.marketing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.marketing.entity.Campaign;
import com.crm.marketing.mapper.CampaignMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 营销活动自动匹配服务
 */
@Service
@RequiredArgsConstructor
public class CampaignMatchService {

    private final CampaignMapper campaignMapper;

    /**
     * 根据客户标签自动匹配活动
     * @param customerTags 客户标签（如 "VIP", "互联网", "电商"）
     * @return 匹配的活动列表
     */
    public List<Campaign> matchCampaigns(List<String> customerTags) {
        // 查找所有进行中的活动
        List<Campaign> activeCampaigns = campaignMapper.selectList(
                new LambdaQueryWrapper<Campaign>()
                        .eq(Campaign::getStatus, 1)); // 进行中

        return activeCampaigns.stream()
                .filter(campaign -> {
                    if (campaign.getTargetAudience() == null) return false;
                    // 检查活动目标群体是否与客户标签匹配
                    return customerTags.stream()
                            .anyMatch(tag -> campaign.getTargetAudience().contains(tag));
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取营销活动统计
     */
    public Map<String, Object> getCampaignStats() {
        Map<String, Object> stats = new LinkedHashMap<>();

        Long total = campaignMapper.selectCount(new LambdaQueryWrapper<>());
        Long active = campaignMapper.selectCount(
                new LambdaQueryWrapper<Campaign>().eq(Campaign::getStatus, 1));
        Long completed = campaignMapper.selectCount(
                new LambdaQueryWrapper<Campaign>().eq(Campaign::getStatus, 2));
        Long draft = campaignMapper.selectCount(
                new LambdaQueryWrapper<Campaign>().eq(Campaign::getStatus, 0));

        stats.put("total", total);
        stats.put("active", active);
        stats.put("completed", completed);
        stats.put("draft", draft);
        return stats;
    }
}

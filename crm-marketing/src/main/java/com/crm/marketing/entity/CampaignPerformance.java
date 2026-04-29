package com.crm.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 营销活动效果明细。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_campaign_performance")
public class CampaignPerformance extends BaseEntity {

    private Long campaignId;
    private String campaignName;
    private String channel;
    private Integer sentCount;
    private Integer openCount;
    private Integer clickCount;
    private Integer conversionCount;
    private BigDecimal costAmount;
    private BigDecimal revenueAmount;
    private String statMonth;
}

package com.crm.marketing.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 营销活动实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_campaign")
public class Campaign extends BaseEntity {

    /** 活动名称 */
    private String name;

    /** 活动类型 (1-邮件, 2-短信, 3-推送, 4-其他) */
    private Integer type;

    /** 目标客户群体 */
    private String targetAudience;

    /** 活动内容 */
    private String content;

    /** 状态 (0-草稿, 1-进行中, 2-已完成, 3-已取消) */
    private Integer status;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;
}

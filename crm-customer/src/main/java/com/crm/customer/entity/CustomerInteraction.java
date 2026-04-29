package com.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户互动记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_customer_interaction")
public class CustomerInteraction extends BaseEntity {

    /** 关联客户ID */
    private Long contactId;

    /** 互动类型 (call/email/ticket/website/meeting/other) */
    private String interactionType;

    /** 标题 */
    private String title;

    /** 描述 */
    private String description;

    /** 时间线展示类型 */
    private String timelineType;
}

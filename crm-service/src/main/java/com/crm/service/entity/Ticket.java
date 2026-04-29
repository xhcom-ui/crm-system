package com.crm.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 服务工单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_ticket")
public class Ticket extends BaseEntity {

    /** 工单编号 */
    private String ticketNo;

    /** 工单标题 */
    private String title;

    /** 关联客户ID */
    private Long contactId;

    /** 工单类型 (1-咨询, 2-投诉, 3-建议, 4-其他) */
    private Integer type;

    /** 优先级 (1-低, 2-中, 3-高, 4-紧急) */
    private Integer priority;

    /** 状态 (1-待处理, 2-处理中, 3-已解决, 4-已关闭) */
    private Integer status;

    /** 处理人ID */
    private Long assigneeId;

    /** 工单内容 */
    private String content;
}

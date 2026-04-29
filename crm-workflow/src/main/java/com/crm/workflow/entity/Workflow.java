package com.crm.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作流实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_workflow")
public class Workflow extends BaseEntity {

    /** 工作流名称 */
    private String name;

    /** 工作流类型 (1-线索分配, 2-合同审批, 3-工单流转, 4-其他) */
    private Integer type;

    /** 触发条件 (JSON格式) */
    private String triggerCondition;

    /** 执行动作 (JSON格式) */
    private String action;

    /** 状态 (0-禁用, 1-启用) */
    private Integer status;

    /** 优先级 */
    private Integer priority;

    /** 描述 */
    private String description;
}

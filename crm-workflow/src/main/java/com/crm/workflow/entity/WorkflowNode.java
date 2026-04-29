package com.crm.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作流节点/步骤实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_workflow_node")
public class WorkflowNode extends BaseEntity {

    /** 所属工作流ID */
    private Long workflowId;

    /** 步骤序号 (从1开始) */
    private Integer stepOrder;

    /** 步骤名称 */
    private String stepName;

    /** 步骤类型 (1-审批, 2-通知, 3-自动操作, 4-条件分支) */
    private Integer stepType;

    /** 处理人类型 (1-指定人, 2-角色) */
    private Integer assigneeType;

    /** 处理人值 (用户ID或角色名) */
    private String assigneeValue;

    /** 步骤配置 (JSON格式，如审批条件、通知模板等) */
    private String stepConfig;
}

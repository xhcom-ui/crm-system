package com.crm.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作流执行实例
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_workflow_instance")
public class WorkflowInstance extends BaseEntity {

    /** 关联工作流ID */
    private Long workflowId;

    /** 工作流名称 (冗余，便于查询) */
    private String workflowName;

    /** 触发类型 (1-手动, 2-自动) */
    private Integer triggerType;

    /** 执行状态 (1-进行中, 2-已完成, 3-已取消, 4-失败) */
    private Integer status;

    /** 当前节点序号 */
    private Integer currentNodeOrder;

    /** 当前节点名称 */
    private String currentNodeName;

    /** 执行结果 (JSON格式) */
    private String result;

    /** 启动参数 (JSON格式) */
    private String startParams;
}

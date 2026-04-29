package com.crm.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工作流实例执行轨迹。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_workflow_trace")
public class WorkflowTrace extends BaseEntity {

    private Long instanceId;
    private Integer stepOrder;
    private String stepName;
    private String stepStatus;
    private String description;
    private String operatorName;
}

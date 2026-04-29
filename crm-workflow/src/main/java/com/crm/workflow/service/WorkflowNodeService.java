package com.crm.workflow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.workflow.entity.WorkflowNode;

import java.util.List;

public interface WorkflowNodeService extends IService<WorkflowNode> {

    /** 根据工作流ID获取有序节点列表 */
    List<WorkflowNode> listByWorkflowId(Long workflowId);

    /** 批量保存节点（按顺序替换） */
    boolean saveNodes(Long workflowId, List<WorkflowNode> nodes);

    /** 调整节点顺序（上移/下移） */
    boolean reorder(Long nodeId, String direction);
}

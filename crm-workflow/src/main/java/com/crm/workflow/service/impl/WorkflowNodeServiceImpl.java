package com.crm.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.workflow.entity.WorkflowNode;
import com.crm.workflow.mapper.WorkflowNodeMapper;
import com.crm.workflow.service.WorkflowNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowNodeServiceImpl extends ServiceImpl<WorkflowNodeMapper, WorkflowNode> implements WorkflowNodeService {

    @Override
    public List<WorkflowNode> listByWorkflowId(Long workflowId) {
        return list(new LambdaQueryWrapper<WorkflowNode>()
                .eq(WorkflowNode::getWorkflowId, workflowId)
                .orderByAsc(WorkflowNode::getStepOrder));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveNodes(Long workflowId, List<WorkflowNode> nodes) {
        // 删除旧节点
        remove(new LambdaQueryWrapper<WorkflowNode>()
                .eq(WorkflowNode::getWorkflowId, workflowId));
        // 重新设置序号并批量保存
        for (int i = 0; i < nodes.size(); i++) {
            nodes.get(i).setWorkflowId(workflowId);
            nodes.get(i).setStepOrder(i + 1);
        }
        return saveBatch(nodes);
    }

    @Override
    public boolean reorder(Long nodeId, String direction) {
        WorkflowNode node = getById(nodeId);
        if (node == null) return false;

        int currentOrder = node.getStepOrder();
        int targetOrder = "up".equals(direction) ? currentOrder - 1 : currentOrder + 1;

        if (targetOrder < 1) return false;

        // 查找目标位置的节点
        WorkflowNode targetNode = getOne(new LambdaQueryWrapper<WorkflowNode>()
                .eq(WorkflowNode::getWorkflowId, node.getWorkflowId())
                .eq(WorkflowNode::getStepOrder, targetOrder));

        if (targetNode == null) return false;

        // 交换序号
        node.setStepOrder(targetOrder);
        targetNode.setStepOrder(currentOrder);
        return updateBatchById(List.of(node, targetNode));
    }
}

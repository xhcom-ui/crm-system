package com.crm.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.crm.common.result.Result;
import com.crm.workflow.entity.WorkflowNode;
import com.crm.workflow.service.WorkflowNodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/workflow/node")
@RequiredArgsConstructor
public class WorkflowNodeController {

    private final WorkflowNodeService workflowNodeService;

    /** 获取工作流的所有节点（按序号排序） */
    @GetMapping("/list/{workflowId}")
    public Result<List<WorkflowNode>> listByWorkflowId(@PathVariable Long workflowId) {
        return Result.success(workflowNodeService.listByWorkflowId(workflowId));
    }

    /** 新增节点 */
    @PostMapping
    @SaCheckPermission("workflow:add")
    public Result<Boolean> save(@Valid @RequestBody WorkflowNode node) {
        // 自动设置序号为当前最大值+1
        List<WorkflowNode> existing = workflowNodeService.listByWorkflowId(node.getWorkflowId());
        node.setStepOrder(existing.size() + 1);
        return Result.success(workflowNodeService.save(node));
    }

    /** 更新节点 */
    @PutMapping("/{id}")
    @SaCheckPermission("workflow:edit")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody WorkflowNode node) {
        node.setId(id);
        return Result.success(workflowNodeService.updateById(node));
    }

    /** 删除节点 */
    @DeleteMapping("/{id}")
    @SaCheckPermission("workflow:del")
    public Result<Boolean> delete(@PathVariable Long id) {
        WorkflowNode node = workflowNodeService.getById(id);
        if (node == null) return Result.error("节点不存在");

        boolean ok = workflowNodeService.removeById(id);
        if (ok) {
            // 重新整理剩余节点的序号
            List<WorkflowNode> nodes = workflowNodeService.listByWorkflowId(node.getWorkflowId());
            for (int i = 0; i < nodes.size(); i++) {
                nodes.get(i).setStepOrder(i + 1);
            }
            workflowNodeService.updateBatchById(nodes);
        }
        return Result.success(ok);
    }

    /** 调整节点顺序 (direction: up/down) */
    @PutMapping("/{id}/reorder")
    public Result<Boolean> reorder(@PathVariable Long id, @RequestParam String direction) {
        return Result.success(workflowNodeService.reorder(id, direction));
    }

    /** 批量保存节点（替换整个工作流的节点列表） */
    @PostMapping("/batch/{workflowId}")
    public Result<Boolean> saveBatch(@PathVariable Long workflowId, @RequestBody List<WorkflowNode> nodes) {
        return Result.success(workflowNodeService.saveNodes(workflowId, nodes));
    }
}

package com.crm.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.log.OperationLog;
import com.crm.common.result.Result;
import com.crm.workflow.entity.Workflow;
import com.crm.workflow.entity.WorkflowInstance;
import com.crm.workflow.entity.WorkflowNode;
import com.crm.workflow.entity.WorkflowTrace;
import com.crm.workflow.feign.LeadsFeignClient;
import com.crm.workflow.feign.dto.LeadDTO;
import com.crm.workflow.mapper.WorkflowTraceMapper;
import com.crm.workflow.service.WorkflowInstanceService;
import com.crm.workflow.service.WorkflowNodeService;
import com.crm.workflow.service.WorkflowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/workflow/workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private final WorkflowService workflowService;
    private final WorkflowNodeService workflowNodeService;
    private final WorkflowInstanceService workflowInstanceService;
    private final LeadsFeignClient leadsFeignClient;
    private final WorkflowTraceMapper traceMapper;

    @GetMapping("/page")
    @SaCheckPermission("workflow:list")
    public Result<IPage<Workflow>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(workflowService.pageWorkflows(new Page<>(current, size), keyword));
    }

    @GetMapping("/{id}")
    public Result<Workflow> getById(@PathVariable Long id) {
        return Result.success(workflowService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("workflow:add")
    public Result<Boolean> save(@Valid @RequestBody Workflow workflow) {
        return Result.success(workflowService.save(workflow));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("workflow:edit")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody Workflow workflow) {
        workflow.setId(id);
        return Result.success(workflowService.updateById(workflow));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("workflow:del")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(workflowService.removeById(id));
    }

    /**
     * 触发工作流执行 - 创建实例并逐步推进节点
     */
    @PostMapping("/{id}/trigger")
    @SaCheckPermission("workflow:trigger")
    @OperationLog(title = "触发工作流", type = "INSERT")
    public Result<Map<String, Object>> trigger(@PathVariable Long id) {
        Workflow workflow = workflowService.getById(id);
        if (workflow == null || workflow.getStatus() == 0) {
            return Result.error("工作流不存在或已禁用");
        }

        List<WorkflowNode> nodes = workflowNodeService.listByWorkflowId(id);
        if (nodes.isEmpty()) {
            return Result.error("工作流没有任何节点，请先设计流程");
        }

        // 创建执行实例
        WorkflowInstance instance = new WorkflowInstance();
        instance.setWorkflowId(workflow.getId());
        instance.setWorkflowName(workflow.getName());
        instance.setTriggerType(1); // 手动触发
        instance.setStatus(1); // 进行中
        instance.setCurrentNodeOrder(1);
        instance.setCurrentNodeName(nodes.get(0).getStepName());
        workflowInstanceService.save(instance);

        Map<String, Object> result = new HashMap<>();
        result.put("instanceId", instance.getId());
        result.put("workflowId", workflow.getId());
        result.put("workflowName", workflow.getName());
        result.put("totalNodes", nodes.size());
        result.put("currentNode", nodes.get(0).getStepName());

        // 根据工作流类型执行对应逻辑
        if (workflow.getType() != null && workflow.getType() == 1) {
            try {
                LeadDTO leadDTO = new LeadDTO();
                leadDTO.setStatus(2);
                leadDTO.setAssigneeId(1L);
                result.put("action", "线索分配");
                result.put("triggered", true);
            } catch (Exception e) {
                result.put("error", e.getMessage());
                instance.setStatus(4); // 失败
                instance.setResult("{\"error\":\"" + e.getMessage() + "\"}");
                workflowInstanceService.updateById(instance);
            }
        } else {
            result.put("action", "其他类型工作流");
        }

        // 更新实例结果
        instance.setResult(result.toString());
        workflowInstanceService.updateById(instance);
        saveTrace(instance.getId(), 1, "触发流程", "finished", "手动触发工作流");
        saveTrace(instance.getId(), 2, nodes.get(0).getStepName(), "processing", "进入首个处理节点");

        return Result.success(result);
    }

    /** 查询工作流执行实例列表 */
    @GetMapping("/instances")
    @SaCheckPermission("workflow:list")
    public Result<IPage<WorkflowInstance>> instances(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long workflowId) {
        return Result.success(workflowInstanceService.pageInstances(
                new Page<>(current, size), workflowId));
    }

    /** 查询单个执行实例详情 */
    @GetMapping("/instances/{id}")
    @SaCheckPermission("workflow:list")
    public Result<WorkflowInstance> instanceDetail(@PathVariable Long id) {
        return Result.success(workflowInstanceService.getById(id));
    }

    /** 查询实例执行轨迹 */
    @GetMapping("/instances/{id}/trace")
    @SaCheckPermission("workflow:list")
    public Result<Map<String, Object>> instanceTrace(@PathVariable Long id) {
        WorkflowInstance instance = workflowInstanceService.getById(id);
        if (instance == null) {
            return Result.error("工作流实例不存在");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("instance", instance);
        result.put("steps", traceSteps(instance));
        result.put("activeStep", Math.max((instance.getCurrentNodeOrder() == null ? 1 : instance.getCurrentNodeOrder()) - 1, 0));
        return Result.success(result);
    }

    @PostMapping("/instances/{id}/approve")
    @SaCheckPermission("workflow:approve")
    @OperationLog(title = "审批通过工作流节点", type = "UPDATE")
    public Result<Boolean> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        return Result.success(handleInstanceAction(id, "approved", "审批通过", body));
    }

    @PostMapping("/instances/{id}/reject")
    @SaCheckPermission("workflow:approve")
    @OperationLog(title = "驳回工作流节点", type = "UPDATE")
    public Result<Boolean> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        WorkflowInstance instance = workflowInstanceService.getById(id);
        if (instance == null) return Result.error("工作流实例不存在");
        instance.setStatus(3);
        instance.setResult("{\"action\":\"reject\"}");
        workflowInstanceService.updateById(instance);
        saveTrace(id, nextStepOrder(id), "驳回", "rejected", reason(body, "审批驳回"));
        return Result.success(true);
    }

    @PostMapping("/instances/{id}/transfer")
    @SaCheckPermission("workflow:approve")
    @OperationLog(title = "转交工作流节点", type = "UPDATE")
    public Result<Boolean> transfer(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        return Result.success(handleInstanceAction(id, "transferred", "节点转交", body));
    }

    @PostMapping("/instances/{id}/retry")
    @SaCheckPermission("workflow:trigger")
    @OperationLog(title = "重试工作流实例", type = "UPDATE")
    public Result<Boolean> retry(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        WorkflowInstance instance = workflowInstanceService.getById(id);
        if (instance == null) return Result.error("工作流实例不存在");
        instance.setStatus(1);
        workflowInstanceService.updateById(instance);
        saveTrace(id, nextStepOrder(id), "重试", "processing", reason(body, "重新进入处理队列"));
        return Result.success(true);
    }

    private List<Map<String, Object>> buildTraceSteps(WorkflowInstance instance) {
        int current = instance.getCurrentNodeOrder() == null ? 1 : instance.getCurrentNodeOrder();
        List<Map<String, Object>> steps = new ArrayList<>();
        steps.add(step("触发流程", "创建流程实例并写入上下文", current > 1));
        steps.add(step("条件判断", "根据业务类型匹配审批路径", current > 2));
        steps.add(step(instance.getCurrentNodeName() == null ? "当前节点" : instance.getCurrentNodeName(), "当前待处理节点", instance.getStatus() != null && instance.getStatus() == 2));
        steps.add(step("完成归档", "写入结果并通知相关人员", instance.getStatus() != null && instance.getStatus() == 2));
        return steps;
    }

    private List<Map<String, Object>> traceSteps(WorkflowInstance instance) {
        try {
            List<WorkflowTrace> traces = traceMapper.selectList(new LambdaQueryWrapper<WorkflowTrace>()
                    .eq(WorkflowTrace::getInstanceId, instance.getId())
                    .orderByAsc(WorkflowTrace::getStepOrder));
            if (!traces.isEmpty()) {
                return traces.stream()
                        .map(trace -> step(trace.getStepName(), trace.getDescription(), "finished".equals(trace.getStepStatus())))
                        .toList();
            }
        } catch (Exception ignored) {
            // 兼容未执行新增表迁移的环境。
        }
        return buildTraceSteps(instance);
    }

    private Map<String, Object> step(String title, String desc, boolean finished) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", title);
        item.put("desc", finished ? desc + "，已完成" : desc);
        item.put("finished", finished);
        return item;
    }

    private void saveTrace(Long instanceId, Integer stepOrder, String stepName, String status, String description) {
        try {
            WorkflowTrace trace = new WorkflowTrace();
            trace.setInstanceId(instanceId);
            trace.setStepOrder(stepOrder);
            trace.setStepName(stepName);
            trace.setStepStatus(status);
            trace.setDescription(description);
            traceMapper.insert(trace);
        } catch (Exception ignored) {
            // 兼容未执行新增表迁移的环境。
        }
    }

    private boolean handleInstanceAction(Long id, String status, String stepName, Map<String, Object> body) {
        WorkflowInstance instance = workflowInstanceService.getById(id);
        if (instance == null) return false;
        if ("approved".equals(status)) {
            instance.setCurrentNodeOrder((instance.getCurrentNodeOrder() == null ? 1 : instance.getCurrentNodeOrder()) + 1);
            instance.setStatus(2);
        }
        workflowInstanceService.updateById(instance);
        saveTrace(id, nextStepOrder(id), stepName, status, reason(body, stepName));
        return true;
    }

    private Integer nextStepOrder(Long instanceId) {
        try {
            Long count = traceMapper.selectCount(new LambdaQueryWrapper<WorkflowTrace>().eq(WorkflowTrace::getInstanceId, instanceId));
            return count.intValue() + 1;
        } catch (Exception ignored) {
            return 1;
        }
    }

    private String reason(Map<String, Object> body, String fallback) {
        if (body == null) return fallback;
        Object reason = body.get("reason");
        return reason == null ? fallback : reason.toString();
    }
}

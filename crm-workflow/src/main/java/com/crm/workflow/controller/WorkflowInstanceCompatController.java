package com.crm.workflow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.result.Result;
import com.crm.workflow.entity.WorkflowInstance;
import com.crm.workflow.service.WorkflowInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 兼容旧版前端工作流实例路径。
 */
@RestController
@RequestMapping("/workflow/instance")
@RequiredArgsConstructor
public class WorkflowInstanceCompatController {

    private final WorkflowInstanceService workflowInstanceService;
    private final WorkflowController workflowController;

    @GetMapping("/page")
    @SaCheckPermission("workflow:list")
    public Result<IPage<WorkflowInstance>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long workflowId) {
        return Result.success(workflowInstanceService.pageInstances(new Page<>(current, size), workflowId));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("workflow:list")
    public Result<WorkflowInstance> detail(@PathVariable Long id) {
        return Result.success(workflowInstanceService.getById(id));
    }

    @GetMapping("/{id}/trace")
    @SaCheckPermission("workflow:list")
    public Result<?> trace(@PathVariable Long id) {
        return workflowController.instanceTrace(id);
    }
}

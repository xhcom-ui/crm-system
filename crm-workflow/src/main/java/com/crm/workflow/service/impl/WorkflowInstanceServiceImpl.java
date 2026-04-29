package com.crm.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.workflow.entity.WorkflowInstance;
import com.crm.workflow.mapper.WorkflowInstanceMapper;
import com.crm.workflow.service.WorkflowInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkflowInstanceServiceImpl extends ServiceImpl<WorkflowInstanceMapper, WorkflowInstance> implements WorkflowInstanceService {

    @Override
    public IPage<WorkflowInstance> pageInstances(Page<WorkflowInstance> page, Long workflowId) {
        LambdaQueryWrapper<WorkflowInstance> wrapper = new LambdaQueryWrapper<WorkflowInstance>()
                .eq(workflowId != null, WorkflowInstance::getWorkflowId, workflowId)
                .orderByDesc(WorkflowInstance::getCreateTime);
        return page(page, wrapper);
    }
}

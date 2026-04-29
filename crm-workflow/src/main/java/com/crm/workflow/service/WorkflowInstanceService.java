package com.crm.workflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.workflow.entity.WorkflowInstance;

public interface WorkflowInstanceService extends IService<WorkflowInstance> {

    /** 分页查询工作流实例（按创建时间倒序） */
    IPage<WorkflowInstance> pageInstances(Page<WorkflowInstance> page, Long workflowId);
}

package com.crm.workflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.workflow.entity.Workflow;

public interface WorkflowService extends IService<Workflow> {
    IPage<Workflow> pageWorkflows(Page<Workflow> page, String keyword);
}

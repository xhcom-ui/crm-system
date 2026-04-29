package com.crm.workflow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.workflow.entity.Workflow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WorkflowMapper extends BaseMapper<Workflow> {
}

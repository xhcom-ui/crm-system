package com.crm.leads.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.leads.entity.Lead;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LeadMapper extends BaseMapper<Lead> {
}

package com.crm.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.sales.entity.SalesOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SalesOrderMapper extends BaseMapper<SalesOrder> {
}

package com.crm.sales.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.sales.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}

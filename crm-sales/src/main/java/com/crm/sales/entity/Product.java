package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_product")
public class Product extends BaseEntity {
    private String code;
    private String name;
    private String category;
    private BigDecimal price;
    private Integer stock;
    private Integer status;
    private String remark;
}

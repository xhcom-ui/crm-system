package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_inventory_snapshot")
public class InventorySnapshot extends BaseEntity {
    private String period;
    private Long productId;
    private String productCode;
    private String productName;
    private String warehouseCode;
    private Integer openingQty;
    private Integer inboundQty;
    private Integer outboundQty;
    private Integer closingQty;
    private BigDecimal stockAmount;
    private Integer snapshotType;
}

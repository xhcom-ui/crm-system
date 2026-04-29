package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_inventory_stock")
public class InventoryStock extends BaseEntity {
    private Long productId;
    private String productCode;
    private String productName;
    private String warehouseCode;
    private String warehouseName;
    private Integer openingQty;
    private Integer inboundQty;
    private Integer outboundQty;
    private Integer lockedQty;
    private Integer availableQty;
    private Integer closingQty;
    private BigDecimal unitCost;
    private BigDecimal stockAmount;
    private Integer safetyStock;
    private Integer status;
}

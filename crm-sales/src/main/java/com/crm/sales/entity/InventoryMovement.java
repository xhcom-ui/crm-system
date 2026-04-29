package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_inventory_movement")
public class InventoryMovement extends BaseEntity {
    private String movementNo;
    private Long productId;
    private String productCode;
    private String productName;
    private String warehouseCode;
    private String movementType;
    private Integer quantity;
    private BigDecimal unitCost;
    private BigDecimal amount;
    private String bizNo;
    private String operatorName;
    private LocalDateTime movementTime;
    private String remark;
}

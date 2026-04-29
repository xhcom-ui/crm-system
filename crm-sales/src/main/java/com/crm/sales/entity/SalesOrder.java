package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_sales_order")
public class SalesOrder extends BaseEntity {
    private String orderNo;
    private Long contactId;
    private String customerName;
    private Long productId;
    private String productName;
    private BigDecimal amount;
    private String platform;
    private String platformOrderNo;
    private String orderType;
    private BigDecimal refundAmount;
    private Integer refundStatus;
    private LocalDateTime paidTime;
    private LocalDateTime extractedAt;
    private Long ownerId;
    private String ownerName;
    private LocalDate signDate;
    private Integer status;
    private String remark;
}

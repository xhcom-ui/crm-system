package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_sales_platform_refund")
public class SalesPlatformRefund extends BaseEntity {
    private String refundNo;
    private String orderNo;
    private String platformOrderNo;
    private String platformCode;
    private String platformName;
    private String shopName;
    private BigDecimal amount;
    private String reason;
    private Integer status;
    private LocalDateTime refundTime;
    private String rawPayload;
}

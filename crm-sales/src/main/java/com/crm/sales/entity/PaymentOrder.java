package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_payment_order")
public class PaymentOrder extends BaseEntity {
    private String paymentNo;
    private String merchantOrderNo;
    private Long salesOrderId;
    private String customerName;
    private String channel;
    private String payMethod;
    private BigDecimal amount;
    private BigDecimal refundableAmount;
    private Integer status;
    private LocalDateTime paidTime;
    private LocalDateTime closedTime;
    private String notifyStatus;
    private String reconcileStatus;
    private String remark;
}

package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_payment_refund")
public class PaymentRefund extends BaseEntity {
    private String refundNo;
    private String paymentNo;
    private BigDecimal amount;
    private String reason;
    private Integer status;
    private LocalDateTime successTime;
}

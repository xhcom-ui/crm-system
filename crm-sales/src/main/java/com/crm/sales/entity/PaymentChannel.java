package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_payment_channel")
public class PaymentChannel extends BaseEntity {
    private String channelCode;
    private String channelName;
    private String payMethod;
    private BigDecimal feeRate;
    private Integer status;
    private String configStatus;
    private String remark;
}

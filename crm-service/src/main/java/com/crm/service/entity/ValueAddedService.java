package com.crm.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_value_added_service")
public class ValueAddedService extends BaseEntity {
    private String name;
    private Long contactId;
    private String customerName;
    private String ownerName;
    private BigDecimal amount;
    private LocalDate expireDate;
    private Integer status;
    private String remark;
}

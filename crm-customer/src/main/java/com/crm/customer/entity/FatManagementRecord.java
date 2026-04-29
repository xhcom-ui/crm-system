package com.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_fat_management_record")
public class FatManagementRecord extends BaseEntity {
    private Long contactId;
    private String customerName;
    private Integer gender;
    private Integer age;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private BigDecimal bodyFatRate;
    private BigDecimal waistCm;
    private BigDecimal visceralFat;
    private BigDecimal basalMetabolism;
    private BigDecimal targetWeightKg;
    private BigDecimal targetBodyFatRate;
    private LocalDate startDate;
    private LocalDate reviewDate;
    private String stage;
    private String planName;
    private String dietAdvice;
    private String exerciseAdvice;
    private String riskLevel;
    private Integer status;
}

package com.crm.leads.feign.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商机 DTO（供线索转化时创建商机使用）
 */
@Data
public class OpportunityDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Long contactId;
    private BigDecimal amount;
    private Integer stage;
    private LocalDate expectedCloseDate;
    private Long ownerId;
    private String remark;
}

package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商机实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_opportunity")
public class Opportunity extends BaseEntity {

    /** 商机名称 */
    private String name;

    /** 关联客户ID */
    private Long contactId;

    /** 预计成交金额 */
    private BigDecimal amount;

    /** 销售阶段 (1-初步接触, 2-需求分析, 3-方案报价, 4-谈判, 5-赢单, 6-输单) */
    private Integer stage;

    /** 预计成交日期 */
    private LocalDate expectedCloseDate;

    /** 负责人ID */
    private Long ownerId;

    /** 备注 */
    private String remark;
}

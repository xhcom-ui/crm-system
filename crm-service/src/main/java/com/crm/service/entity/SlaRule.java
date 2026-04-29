package com.crm.service.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_sla_rule")
public class SlaRule extends BaseEntity {
    private String title;
    private Integer priority;
    private Integer responseMinutes;
    private String description;
    private String tone;
    private Integer status;
}

package com.crm.leads.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 销售线索实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_lead")
public class Lead extends BaseEntity {

    /** 线索名称 */
    private String name;

    /** 联系方式 */
    private String contactInfo;

    /** 线索来源 (1-官网, 2-广告, 3-推荐, 4-社交媒体, 5-其他) */
    private Integer source;

    /** 线索评分 (0-100) */
    private Integer score;

    /** 状态 (1-新建, 2-已分配, 3-已转化, 4-已关闭) */
    private Integer status;

    /** 分配人ID */
    private Long assigneeId;

    /** 备注 */
    private String remark;
}

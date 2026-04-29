package com.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 客户联系人实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_contact")
public class Contact extends BaseEntity {

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 公司名称 */
    private String company;

    /** 职位 */
    private String position;

    /** 客户来源 (1-网站, 2-推荐, 3-展会, 4-其他) */
    private Integer source;

    /** 客户等级 (1-普通, 2-重要, 3-VIP) */
    private Integer level;

    /** 备注 */
    private String remark;
}

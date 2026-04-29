package com.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("crm_customer_info_item")
public class CustomerInfoItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String moduleType;
    private String customerName;
    private String title;
    private String relation;
    private String target;
    private String identityType;
    private String identityNo;
    private String birthday;
    private String gender;
    private String email;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String address;
    private String website;
    private String blog;
    private String contentPreference;
    private String contactType;
    private String description;
    private String eventTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

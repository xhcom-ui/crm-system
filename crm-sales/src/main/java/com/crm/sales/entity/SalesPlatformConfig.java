package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_sales_platform_config")
public class SalesPlatformConfig extends BaseEntity {
    private String platformCode;
    private String platformName;
    private String shopName;
    private String appKey;
    private String appSecret;
    private String accessToken;
    private String apiEndpoint;
    private Integer status;
    private LocalDateTime lastSyncTime;
    private String remark;
}

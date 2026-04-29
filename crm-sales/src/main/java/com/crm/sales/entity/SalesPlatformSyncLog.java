package com.crm.sales.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_sales_platform_sync_log")
public class SalesPlatformSyncLog extends BaseEntity {
    private Long configId;
    private String platformCode;
    private String platformName;
    private String syncType;
    private Integer orderCount;
    private Integer refundCount;
    private Integer status;
    private String message;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
}

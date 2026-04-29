package com.crm.customer.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("crm_followup_record")
public class FollowupRecord extends BaseEntity {
    private Long contactId;
    private String customerName;
    private String title;
    private String content;
    private String ownerName;
    private LocalDateTime followTime;
    private String nextAction;
    private String timelineType;
}

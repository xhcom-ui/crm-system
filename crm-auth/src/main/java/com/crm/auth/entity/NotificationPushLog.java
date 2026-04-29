package com.crm.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知渠道推送日志
 */
@Data
@TableName("sys_notification_push_log")
public class NotificationPushLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long notificationId;

    private String channel;

    private String channelName;

    private String target;

    private String status;

    private String response;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

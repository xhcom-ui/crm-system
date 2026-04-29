package com.crm.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知渠道配置
 */
@Data
@TableName("sys_notification_channel_config")
public class NotificationChannelConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String channel;

    private String channelName;

    private Integer enabled;

    private String webhookUrl;

    private String secret;

    private String smtpHost;

    private Integer smtpPort;

    private String smtpUsername;

    private String smtpPassword;

    private String senderAddress;

    private String receiver;

    private String template;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

package com.crm.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知记录
 */
@Data
@TableName("sys_notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 通知标题 */
    private String title;

    /** 通知内容 */
    private String message;

    /** 通知类型 */
    private String type;

    /** 发送者 */
    private String sender;

    /** 推送渠道，逗号分隔 */
    private String channels;

    /** 渠道推送摘要 */
    private String pushSummary;

    /** 发送时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

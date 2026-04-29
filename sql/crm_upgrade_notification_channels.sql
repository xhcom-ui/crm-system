USE crm_auth;

SET @has_channels = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'crm_auth' AND TABLE_NAME = 'sys_notification' AND COLUMN_NAME = 'channels'
);
SET @ddl = IF(@has_channels = 0,
    'ALTER TABLE sys_notification ADD COLUMN channels VARCHAR(200) DEFAULT NULL COMMENT ''推送渠道，逗号分隔''',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has_push_summary = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'crm_auth' AND TABLE_NAME = 'sys_notification' AND COLUMN_NAME = 'push_summary'
);
SET @ddl = IF(@has_push_summary = 0,
    'ALTER TABLE sys_notification ADD COLUMN push_summary VARCHAR(300) DEFAULT NULL COMMENT ''渠道推送摘要''',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS sys_notification_channel_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    channel VARCHAR(30) NOT NULL COMMENT '渠道编码',
    channel_name VARCHAR(50) NOT NULL COMMENT '渠道名称',
    enabled TINYINT DEFAULT 0 COMMENT '是否启用',
    webhook_url VARCHAR(500) DEFAULT NULL COMMENT 'Webhook 或 API 地址',
    secret VARCHAR(200) DEFAULT NULL COMMENT '签名密钥/接口密钥',
    smtp_host VARCHAR(120) DEFAULT NULL COMMENT 'SMTP 地址',
    smtp_port INT DEFAULT NULL COMMENT 'SMTP 端口',
    smtp_username VARCHAR(120) DEFAULT NULL COMMENT 'SMTP 账号',
    smtp_password VARCHAR(200) DEFAULT NULL COMMENT 'SMTP 密码',
    sender_address VARCHAR(120) DEFAULT NULL COMMENT '发件人',
    receiver VARCHAR(500) DEFAULT NULL COMMENT '默认接收人，多个用逗号分隔',
    template TEXT DEFAULT NULL COMMENT '消息模板，支持 ${title}/${message}/${type}',
    remark VARCHAR(300) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_channel (channel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知渠道配置表';

CREATE TABLE IF NOT EXISTS sys_notification_push_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    notification_id BIGINT NOT NULL COMMENT '通知ID',
    channel VARCHAR(30) NOT NULL COMMENT '渠道编码',
    channel_name VARCHAR(50) DEFAULT NULL COMMENT '渠道名称',
    target VARCHAR(500) DEFAULT NULL COMMENT '推送目标',
    status VARCHAR(20) NOT NULL COMMENT '状态 SUCCESS/SKIPPED/FAILED',
    response VARCHAR(600) DEFAULT NULL COMMENT '响应或错误',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_notification_id (notification_id),
    INDEX idx_channel (channel),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知渠道推送日志';

INSERT INTO sys_notification_channel_config
(channel, channel_name, enabled, template, remark)
VALUES
('email', '邮件', 0, '【${title}】${message}', '配置 SMTP 与收件人后推送邮件'),
('dingtalk', '钉钉', 0, '【${title}】${message}', '配置钉钉机器人 Webhook 后推送'),
('feishu', '飞书', 0, '【${title}】${message}', '配置飞书机器人 Webhook 后推送'),
('sms', '短信', 0, '【${title}】${message}', '配置短信服务 API 地址和手机号后推送'),
('wechat', '微信', 0, '【${title}】${message}', '配置企业微信机器人 Webhook 后推送')
ON DUPLICATE KEY UPDATE channel_name = VALUES(channel_name);

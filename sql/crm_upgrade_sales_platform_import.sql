USE crm_sales;

DELIMITER $$
DROP PROCEDURE IF EXISTS add_sales_order_col $$
CREATE PROCEDURE add_sales_order_col(IN col_name VARCHAR(64), IN ddl_sql TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'crm_sales_order' AND COLUMN_NAME = col_name
    ) THEN
        SET @sql = ddl_sql;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$
DROP PROCEDURE IF EXISTS add_sales_order_idx $$
CREATE PROCEDURE add_sales_order_idx(IN idx_name VARCHAR(64), IN ddl_sql TEXT)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'crm_sales_order' AND INDEX_NAME = idx_name
    ) THEN
        SET @sql = ddl_sql;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$
DELIMITER ;

CALL add_sales_order_col('platform', 'ALTER TABLE crm_sales_order ADD COLUMN platform VARCHAR(50) DEFAULT NULL COMMENT ''来源平台''');
CALL add_sales_order_col('platform_order_no', 'ALTER TABLE crm_sales_order ADD COLUMN platform_order_no VARCHAR(100) DEFAULT NULL COMMENT ''平台订单号''');
CALL add_sales_order_col('order_type', 'ALTER TABLE crm_sales_order ADD COLUMN order_type VARCHAR(30) DEFAULT ''ORDER'' COMMENT ''订单类型 ORDER/REFUND''');
CALL add_sales_order_col('refund_amount', 'ALTER TABLE crm_sales_order ADD COLUMN refund_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT ''退款金额''');
CALL add_sales_order_col('refund_status', 'ALTER TABLE crm_sales_order ADD COLUMN refund_status TINYINT DEFAULT 0 COMMENT ''退款状态 0-无退款 1-处理中 2-已退款''');
CALL add_sales_order_col('paid_time', 'ALTER TABLE crm_sales_order ADD COLUMN paid_time DATETIME DEFAULT NULL COMMENT ''支付时间''');
CALL add_sales_order_col('extracted_at', 'ALTER TABLE crm_sales_order ADD COLUMN extracted_at DATETIME DEFAULT NULL COMMENT ''平台抽取时间''');
CALL add_sales_order_idx('idx_platform_order_no', 'ALTER TABLE crm_sales_order ADD INDEX idx_platform_order_no (platform_order_no)');
CALL add_sales_order_idx('idx_platform', 'ALTER TABLE crm_sales_order ADD INDEX idx_platform (platform)');
DROP PROCEDURE IF EXISTS add_sales_order_col;
DROP PROCEDURE IF EXISTS add_sales_order_idx;

CREATE TABLE IF NOT EXISTS crm_sales_platform_config (
    id BIGINT NOT NULL COMMENT '主键ID',
    platform_code VARCHAR(50) NOT NULL COMMENT '平台编码',
    platform_name VARCHAR(80) NOT NULL COMMENT '平台名称',
    shop_name VARCHAR(120) NOT NULL COMMENT '店铺名称',
    app_key VARCHAR(200) DEFAULT NULL COMMENT 'AppKey',
    app_secret VARCHAR(500) DEFAULT NULL COMMENT 'AppSecret',
    access_token VARCHAR(1000) DEFAULT NULL COMMENT '访问令牌',
    api_endpoint VARCHAR(300) DEFAULT NULL COMMENT '接口地址',
    status TINYINT DEFAULT 1 COMMENT '状态 0-停用 1-启用',
    last_sync_time DATETIME DEFAULT NULL COMMENT '最近同步时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_platform_code (platform_code),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售平台配置';

CREATE TABLE IF NOT EXISTS crm_sales_platform_refund (
    id BIGINT NOT NULL COMMENT '主键ID',
    refund_no VARCHAR(100) NOT NULL COMMENT '退款编号',
    order_no VARCHAR(80) DEFAULT NULL COMMENT 'CRM订单编号',
    platform_order_no VARCHAR(100) DEFAULT NULL COMMENT '平台订单号',
    platform_code VARCHAR(50) DEFAULT NULL COMMENT '平台编码',
    platform_name VARCHAR(80) DEFAULT NULL COMMENT '平台名称',
    shop_name VARCHAR(120) DEFAULT NULL COMMENT '店铺名称',
    amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '退款金额',
    reason VARCHAR(300) DEFAULT NULL COMMENT '退款原因',
    status TINYINT DEFAULT 1 COMMENT '状态 1-处理中 2-成功 3-失败',
    refund_time DATETIME DEFAULT NULL COMMENT '退款时间',
    raw_payload TEXT DEFAULT NULL COMMENT '原始数据',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_refund_no (refund_no),
    INDEX idx_order_no (order_no),
    INDEX idx_platform_order_no (platform_order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台退款数据';

CREATE TABLE IF NOT EXISTS crm_sales_platform_sync_log (
    id BIGINT NOT NULL COMMENT '主键ID',
    config_id BIGINT DEFAULT NULL COMMENT '平台配置ID',
    platform_code VARCHAR(50) DEFAULT NULL COMMENT '平台编码',
    platform_name VARCHAR(80) DEFAULT NULL COMMENT '平台名称',
    sync_type VARCHAR(30) DEFAULT 'ALL' COMMENT '同步类型 ORDER/REFUND/ALL',
    order_count INT DEFAULT 0 COMMENT '订单数量',
    refund_count INT DEFAULT 0 COMMENT '退款数量',
    status TINYINT DEFAULT 1 COMMENT '状态 1-成功 2-失败',
    message VARCHAR(500) DEFAULT NULL COMMENT '同步结果',
    started_at DATETIME DEFAULT NULL COMMENT '开始时间',
    finished_at DATETIME DEFAULT NULL COMMENT '结束时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_config_id (config_id),
    INDEX idx_started_at (started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台数据抽取日志';

INSERT IGNORE INTO crm_sales_platform_config
(id, platform_code, platform_name, shop_name, app_key, api_endpoint, status, remark)
VALUES
(23001, 'taobao', '淘宝', '淘宝旗舰店', 'demo-taobao-app', 'https://eco.taobao.com/router/rest', 1, '演示配置'),
(23002, 'tmall', '天猫', '天猫旗舰店', 'demo-tmall-app', 'https://eco.taobao.com/router/rest', 1, '演示配置'),
(23003, 'jd', '京东', '京东自营店', 'demo-jd-app', 'https://api.jd.com/routerjson', 1, '演示配置'),
(23004, 'douyin', '抖音', '抖音小店', 'demo-douyin-app', 'https://openapi-fxg.jinritemai.com', 1, '演示配置'),
(23005, 'amazon', '亚马逊', 'Amazon Store', 'demo-amazon-app', 'https://sellingpartnerapi-na.amazon.com', 0, '待授权'),
(23006, 'pdd', '拼多多', '拼多多店铺', 'demo-pdd-app', 'https://gw-api.pinduoduo.com/api/router', 1, '演示配置');

USE crm_auth;

INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, order_num, status, visible)
VALUES
(2201, 102, '导入销售订单', 'F', NULL, NULL, 'sales-management:import', NULL, 10, 1, 0),
(2202, 102, '平台配置', 'F', NULL, NULL, 'sales-platform:config', NULL, 11, 1, 0),
(2203, 102, '平台抽取', 'F', NULL, NULL, 'sales-platform:sync', NULL, 12, 1, 0),
(2204, 102, '平台数据查看', 'F', NULL, NULL, 'sales-platform:list', NULL, 13, 1, 0);

INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE id IN (2201, 2202, 2203, 2204);

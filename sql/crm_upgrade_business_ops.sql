-- =============================================
-- CRM 脂肪管理/支付引擎/库存进销存增量升级脚本
-- 适用于已经执行过 crm_init.sql 和 crm_upgrade_insights.sql 的环境
-- =============================================

USE crm_auth;

INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, order_num, status) VALUES
(201, 0, '脂肪管理', 'C', 'fat-management', NULL, 'fat-management:list', 'DataLine', 13, 1),
(202, 0, '支付管理', 'C', 'payment', NULL, 'payment:list', 'Wallet', 14, 1),
(203, 0, '库存进销存', 'C', 'inventory', NULL, 'inventory:list', 'TakeawayBox', 15, 1),
(204, 201, '脂肪档案新增', 'F', NULL, NULL, 'fat-management:add', NULL, 1, 1),
(205, 201, '脂肪档案编辑', 'F', NULL, NULL, 'fat-management:edit', NULL, 2, 1),
(206, 202, '支付统一下单', 'F', NULL, NULL, 'payment:add', NULL, 1, 1),
(207, 202, '支付退款', 'F', NULL, NULL, 'payment:refund', NULL, 2, 1),
(208, 203, '库存流水新增', 'F', NULL, NULL, 'inventory:movement', NULL, 1, 1);

INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id) VALUES
(201, 1, 201),(202, 1, 202),(203, 1, 203),(204, 1, 204),
(205, 1, 205),(206, 1, 206),(207, 1, 207),(208, 1, 208);

USE crm_customer;

CREATE TABLE IF NOT EXISTS crm_fat_management_record (
    id BIGINT NOT NULL COMMENT '主键ID',
    contact_id BIGINT DEFAULT NULL COMMENT '客户ID',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户姓名',
    gender TINYINT DEFAULT NULL COMMENT '性别',
    age INT DEFAULT NULL COMMENT '年龄',
    height_cm DECIMAL(8,2) DEFAULT NULL COMMENT '身高cm',
    weight_kg DECIMAL(8,2) DEFAULT NULL COMMENT '体重kg',
    body_fat_rate DECIMAL(8,2) DEFAULT NULL COMMENT '体脂率',
    waist_cm DECIMAL(8,2) DEFAULT NULL COMMENT '腰围cm',
    visceral_fat DECIMAL(8,2) DEFAULT NULL COMMENT '内脏脂肪等级',
    basal_metabolism DECIMAL(10,2) DEFAULT NULL COMMENT '基础代谢',
    target_weight_kg DECIMAL(8,2) DEFAULT NULL COMMENT '目标体重',
    target_body_fat_rate DECIMAL(8,2) DEFAULT NULL COMMENT '目标体脂率',
    start_date DATE DEFAULT NULL COMMENT '开始日期',
    review_date DATE DEFAULT NULL COMMENT '复测日期',
    stage VARCHAR(50) DEFAULT NULL COMMENT '阶段',
    plan_name VARCHAR(100) DEFAULT NULL COMMENT '方案名称',
    diet_advice VARCHAR(500) DEFAULT NULL COMMENT '饮食建议',
    exercise_advice VARCHAR(500) DEFAULT NULL COMMENT '运动建议',
    risk_level VARCHAR(20) DEFAULT '中' COMMENT '风险等级',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_contact_id (contact_id),
    INDEX idx_review_date (review_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='脂肪管理档案';

INSERT IGNORE INTO crm_fat_management_record
(id, contact_id, customer_name, gender, age, height_cm, weight_kg, body_fat_rate, waist_cm, visceral_fat, basal_metabolism, target_weight_kg, target_body_fat_rate, start_date, review_date, stage, plan_name, diet_advice, exercise_advice, risk_level, status)
VALUES
(201001, 1, '星河科技-王总', 1, 38, 175.00, 86.50, 28.60, 94.00, 11.00, 1680.00, 76.00, 20.00, '2026-04-01', '2026-05-01', '减脂执行', '商务人士 90 天减脂', '控制精制碳水，增加优质蛋白', '每周 3 次力量训练和 2 次有氧', '中', 1),
(201002, 2, '云启贸易-李女士', 2, 32, 164.00, 68.20, 32.40, 82.00, 9.00, 1380.00, 58.00, 24.00, '2026-04-10', '2026-05-10', '建档评估', '基础体脂管理', '记录三餐热量和饮水', '每天 8000 步，逐步加入抗阻训练', '高', 1),
(201003, 3, '远山教育-陈老师', 1, 41, 172.00, 73.00, 23.10, 86.00, 7.00, 1560.00, 70.00, 19.00, '2026-03-15', '2026-04-30', '维持巩固', '维持期复测方案', '稳定总热量，避免周末暴食', '保持每周 4 次中低强度运动', '低', 2);

USE crm_sales;

CREATE TABLE IF NOT EXISTS crm_payment_order (
    id BIGINT NOT NULL COMMENT '主键ID',
    payment_no VARCHAR(64) NOT NULL COMMENT '支付订单号',
    merchant_order_no VARCHAR(64) DEFAULT NULL COMMENT '商户订单号',
    sales_order_id BIGINT DEFAULT NULL COMMENT '销售订单ID',
    customer_name VARCHAR(100) DEFAULT NULL COMMENT '客户名称',
    channel VARCHAR(50) DEFAULT NULL COMMENT '支付渠道',
    pay_method VARCHAR(50) DEFAULT NULL COMMENT '支付方式',
    amount DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '支付金额',
    refundable_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '可退金额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1待支付 2成功 3关闭 4失败',
    paid_time DATETIME DEFAULT NULL COMMENT '成功时间',
    closed_time DATETIME DEFAULT NULL COMMENT '关闭时间',
    notify_status VARCHAR(30) DEFAULT 'WAITING' COMMENT '通知状态',
    reconcile_status VARCHAR(30) DEFAULT 'PENDING' COMMENT '对账状态',
    remark VARCHAR(500) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_payment_no (payment_no),
    INDEX idx_merchant_order_no (merchant_order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单';

CREATE TABLE IF NOT EXISTS crm_payment_refund (
    id BIGINT NOT NULL COMMENT '主键ID',
    refund_no VARCHAR(64) NOT NULL COMMENT '退款单号',
    payment_no VARCHAR(64) NOT NULL COMMENT '支付订单号',
    amount DECIMAL(15,2) NOT NULL DEFAULT 0.00 COMMENT '退款金额',
    reason VARCHAR(200) DEFAULT NULL COMMENT '退款原因',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1处理中 2成功 3失败',
    success_time DATETIME DEFAULT NULL COMMENT '成功时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_refund_no (refund_no),
    INDEX idx_payment_no (payment_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付退款';

CREATE TABLE IF NOT EXISTS crm_payment_channel (
    id BIGINT NOT NULL COMMENT '主键ID',
    channel_code VARCHAR(50) NOT NULL COMMENT '渠道编码',
    channel_name VARCHAR(100) NOT NULL COMMENT '渠道名称',
    pay_method VARCHAR(50) NOT NULL COMMENT '支付方式',
    fee_rate DECIMAL(8,4) DEFAULT 0.0000 COMMENT '费率',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    config_status VARCHAR(30) DEFAULT 'READY' COMMENT '配置状态',
    remark VARCHAR(500) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_channel_method (channel_code, pay_method)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付渠道';

CREATE TABLE IF NOT EXISTS crm_inventory_stock (
    id BIGINT NOT NULL COMMENT '主键ID',
    product_id BIGINT DEFAULT NULL COMMENT '产品ID',
    product_code VARCHAR(64) NOT NULL COMMENT '产品编码',
    product_name VARCHAR(200) NOT NULL COMMENT '产品名称',
    warehouse_code VARCHAR(50) NOT NULL COMMENT '仓库编码',
    warehouse_name VARCHAR(100) DEFAULT NULL COMMENT '仓库名称',
    opening_qty INT DEFAULT 0 COMMENT '期初库存',
    inbound_qty INT DEFAULT 0 COMMENT '本期入库',
    outbound_qty INT DEFAULT 0 COMMENT '本期出库',
    locked_qty INT DEFAULT 0 COMMENT '锁定库存',
    available_qty INT DEFAULT 0 COMMENT '可用库存',
    closing_qty INT DEFAULT 0 COMMENT '期末库存',
    unit_cost DECIMAL(15,2) DEFAULT 0.00 COMMENT '单位成本',
    stock_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '库存金额',
    safety_stock INT DEFAULT 0 COMMENT '安全库存',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_product_warehouse (product_code, warehouse_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='当前库存';

CREATE TABLE IF NOT EXISTS crm_inventory_snapshot (
    id BIGINT NOT NULL COMMENT '主键ID',
    period VARCHAR(20) NOT NULL COMMENT '期间',
    product_id BIGINT DEFAULT NULL COMMENT '产品ID',
    product_code VARCHAR(64) NOT NULL COMMENT '产品编码',
    product_name VARCHAR(200) NOT NULL COMMENT '产品名称',
    warehouse_code VARCHAR(50) NOT NULL COMMENT '仓库编码',
    opening_qty INT DEFAULT 0 COMMENT '期初',
    inbound_qty INT DEFAULT 0 COMMENT '入库',
    outbound_qty INT DEFAULT 0 COMMENT '出库',
    closing_qty INT DEFAULT 0 COMMENT '期末',
    stock_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '库存金额',
    snapshot_type TINYINT NOT NULL DEFAULT 1 COMMENT '快照类型 1上期 2本期',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_period (period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存期间快照';

CREATE TABLE IF NOT EXISTS crm_inventory_movement (
    id BIGINT NOT NULL COMMENT '主键ID',
    movement_no VARCHAR(64) NOT NULL COMMENT '流水号',
    product_id BIGINT DEFAULT NULL COMMENT '产品ID',
    product_code VARCHAR(64) NOT NULL COMMENT '产品编码',
    product_name VARCHAR(200) NOT NULL COMMENT '产品名称',
    warehouse_code VARCHAR(50) NOT NULL COMMENT '仓库编码',
    movement_type VARCHAR(50) NOT NULL COMMENT '流水类型',
    quantity INT NOT NULL DEFAULT 0 COMMENT '数量',
    unit_cost DECIMAL(15,2) DEFAULT 0.00 COMMENT '单价',
    amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '金额',
    biz_no VARCHAR(64) DEFAULT NULL COMMENT '业务单号',
    operator_name VARCHAR(50) DEFAULT NULL COMMENT '操作人',
    movement_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
    remark VARCHAR(500) DEFAULT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT DEFAULT NULL,
    update_by BIGINT DEFAULT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_movement_no (movement_no),
    INDEX idx_product_code (product_code),
    INDEX idx_movement_time (movement_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水';

INSERT IGNORE INTO crm_payment_channel (id, channel_code, channel_name, pay_method, fee_rate, status, config_status, remark) VALUES
(202001, 'WECHAT', '微信支付', 'WX_NATIVE', 0.0060, 1, 'READY', 'Native 扫码支付'),
(202002, 'ALIPAY', '支付宝', 'ALI_QR', 0.0060, 1, 'READY', '当面付'),
(202003, 'BANK', '企业网银', 'BANK_TRANSFER', 0.0010, 1, 'PENDING', '需补充回单解析');

INSERT IGNORE INTO crm_payment_order (id, payment_no, merchant_order_no, customer_name, channel, pay_method, amount, refundable_amount, status, paid_time, notify_status, reconcile_status, remark) VALUES
(202101, 'P202604280001', 'ORD202604280001', '星河科技有限公司', '微信', 'WX_NATIVE', 98000.00, 98000.00, 2, '2026-04-28 10:30:11', 'SUCCESS', 'MATCHED', 'CRM 标准版支付'),
(202102, 'P202604280002', 'ORD202604280002', '云启贸易', '支付宝', 'ALI_QR', 36000.00, 36000.00, 1, NULL, 'WAITING', 'PENDING', '营销插件待支付'),
(202103, 'P202604270001', 'ORD202604270001', '远山教育', '企业网银', 'BANK_TRANSFER', 52000.00, 50000.00, 2, '2026-04-27 19:50:16', 'SUCCESS', 'DIFF', '服务包支付');

INSERT IGNORE INTO crm_payment_refund (id, refund_no, payment_no, amount, reason, status, success_time) VALUES
(202201, 'R202604280001', 'P202604270001', 2000.00, '服务范围调整', 2, '2026-04-28 11:20:00');

INSERT IGNORE INTO crm_inventory_stock (id, product_id, product_code, product_name, warehouse_code, warehouse_name, opening_qty, inbound_qty, outbound_qty, locked_qty, available_qty, closing_qty, unit_cost, stock_amount, safety_stock, status) VALUES
(203001, 1, 'CRM-SaaS-001', 'CRM 标准版授权', 'MAIN', '主仓', 120, 40, 36, 8, 116, 124, 1800.00, 223200.00, 30, 1),
(203002, 2, 'CRM-AUTO-002', '营销自动化插件', 'MAIN', '主仓', 80, 20, 18, 5, 77, 82, 900.00, 73800.00, 20, 1),
(203003, 3, 'CRM-SVC-003', '数据迁移服务包', 'SERVICE', '服务仓', 25, 10, 22, 2, 11, 13, 1200.00, 15600.00, 15, 1);

INSERT IGNORE INTO crm_inventory_snapshot (id, period, product_id, product_code, product_name, warehouse_code, opening_qty, inbound_qty, outbound_qty, closing_qty, stock_amount, snapshot_type) VALUES
(203101, '2026-03', 1, 'CRM-SaaS-001', 'CRM 标准版授权', 'MAIN', 100, 50, 30, 120, 216000.00, 1),
(203102, '2026-03', 2, 'CRM-AUTO-002', '营销自动化插件', 'MAIN', 70, 25, 15, 80, 72000.00, 1),
(203103, '2026-03', 3, 'CRM-SVC-003', '数据迁移服务包', 'SERVICE', 30, 5, 10, 25, 30000.00, 1),
(203104, '2026-04', 1, 'CRM-SaaS-001', 'CRM 标准版授权', 'MAIN', 120, 40, 36, 124, 223200.00, 2),
(203105, '2026-04', 2, 'CRM-AUTO-002', '营销自动化插件', 'MAIN', 80, 20, 18, 82, 73800.00, 2),
(203106, '2026-04', 3, 'CRM-SVC-003', '数据迁移服务包', 'SERVICE', 25, 10, 22, 13, 15600.00, 2);

INSERT IGNORE INTO crm_inventory_movement (id, movement_no, product_id, product_code, product_name, warehouse_code, movement_type, quantity, unit_cost, amount, biz_no, operator_name, movement_time, remark) VALUES
(203201, 'ST202604280001', 1, 'CRM-SaaS-001', 'CRM 标准版授权', 'MAIN', 'PURCHASE_IN', 40, 1800.00, 72000.00, 'PO202604001', '张三', '2026-04-28 09:20:00', '采购入库'),
(203202, 'ST202604280002', 1, 'CRM-SaaS-001', 'CRM 标准版授权', 'MAIN', 'SALE_OUT', 12, 1800.00, 21600.00, 'SO202604001', '李四', '2026-04-28 10:40:00', '销售出库'),
(203203, 'ST202604270001', 3, 'CRM-SVC-003', '数据迁移服务包', 'SERVICE', 'SALE_OUT', 8, 1200.00, 9600.00, 'SO202604003', '王五', '2026-04-27 15:10:00', '服务包消耗');

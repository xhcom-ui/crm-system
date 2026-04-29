-- =============================================
-- CRM 洞察/知识库/营销效果/工作流轨迹增量升级脚本
-- 适用于已经执行过 crm_init.sql 的环境
-- =============================================

USE crm_auth;

INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, order_num, status) VALUES
(100, 0, '产品管理', 'C', 'product', NULL, 'product:list', 'Box', 4, 1),
(101, 0, '增值服务', 'C', 'value-service', NULL, 'value-service:list', 'Service', 5, 1),
(102, 0, '销售管理', 'C', 'sales-management', NULL, 'sales-management:list', 'Money', 6, 1),
(103, 0, '跟进记录', 'C', 'followup', NULL, 'followup:list', 'Document', 7, 1),
(104, 0, '数据统计', 'C', 'statistics', NULL, 'statistics:list', 'Histogram', 8, 1),
(105, 100, '产品新增', 'F', NULL, NULL, 'product:add', NULL, 1, 1),
(106, 101, '增值服务新增', 'F', NULL, NULL, 'value-service:add', NULL, 1, 1),
(107, 102, '销售订单新增', 'F', NULL, NULL, 'sales-management:add', NULL, 1, 1),
(108, 103, '跟进记录新增', 'F', NULL, NULL, 'followup:add', NULL, 1, 1),
(109, 19, '客户互动新增', 'F', NULL, NULL, 'customer:interaction:add', NULL, 4, 1),
(110, 21, '营销效果录入', 'F', NULL, NULL, 'marketing:performance:add', NULL, 4, 1),
(111, 22, '知识库新增', 'F', NULL, NULL, 'service:knowledge:add', NULL, 4, 1),
(112, 22, 'SLA新增', 'F', NULL, NULL, 'service:sla:add', NULL, 5, 1),
(113, 24, '工作流触发', 'F', NULL, NULL, 'workflow:trigger', NULL, 4, 1),
(114, 24, '工作流审批', 'F', NULL, NULL, 'workflow:approve', NULL, 5, 1);

INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id) VALUES
(100, 1, 100),(101, 1, 101),(102, 1, 102),(103, 1, 103),(104, 1, 104),
(105, 1, 105),(106, 1, 106),(107, 1, 107),(108, 1, 108),(109, 1, 109),
(110, 1, 110),(111, 1, 111),(112, 1, 112),(113, 1, 113),(114, 1, 114);

USE crm_customer;

CREATE TABLE IF NOT EXISTS crm_customer_interaction (
    id BIGINT NOT NULL COMMENT '主键ID',
    contact_id BIGINT NOT NULL COMMENT '关联客户ID',
    interaction_type VARCHAR(30) DEFAULT 'other' COMMENT '互动类型',
    title VARCHAR(200) NOT NULL COMMENT '互动标题',
    description VARCHAR(1000) DEFAULT NULL COMMENT '互动描述',
    timeline_type VARCHAR(30) DEFAULT 'primary' COMMENT '时间线展示类型',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_contact_id (contact_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户互动记录表';

CREATE TABLE IF NOT EXISTS crm_followup_record (
    id BIGINT NOT NULL COMMENT '主键ID',
    contact_id BIGINT DEFAULT NULL COMMENT '客户ID',
    customer_name VARCHAR(200) DEFAULT NULL COMMENT '客户名称',
    title VARCHAR(200) NOT NULL COMMENT '跟进标题',
    content VARCHAR(1000) DEFAULT NULL COMMENT '跟进内容',
    owner_name VARCHAR(100) DEFAULT NULL COMMENT '负责人',
    follow_time DATETIME DEFAULT NULL COMMENT '跟进时间',
    next_action VARCHAR(300) DEFAULT NULL COMMENT '下一步动作',
    timeline_type VARCHAR(30) DEFAULT 'primary' COMMENT '时间线展示类型',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_contact_id (contact_id),
    INDEX idx_follow_time (follow_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户跟进记录表';

INSERT IGNORE INTO crm_contact (id, name, phone, email, company, position, source, level, remark) VALUES
(10001, '王明', '13800000001', 'wangming@example.com', '星河科技有限公司', '采购负责人', 1, 3, '重点跟进客户');

INSERT IGNORE INTO crm_customer_interaction (id, contact_id, interaction_type, title, description, timeline_type) VALUES
(11001, 10001, 'call', '销售电话跟进', '确认采购预算，客户希望下周收到正式报价。', 'primary'),
(11002, 10001, 'email', '营销邮件打开', '打开产品升级邮件并点击案例链接。', 'success'),
(11003, 10001, 'ticket', '服务咨询记录', '咨询数据导入规则，客服已给出处理方案。', 'warning');

INSERT IGNORE INTO crm_followup_record (id, contact_id, customer_name, title, content, owner_name, follow_time, next_action, timeline_type) VALUES
(12001, 10001, '星河科技有限公司', '报价沟通', '客户认可方案，需补充实施周期和付款节点。', '张三', '2026-04-28 10:20:00', '明天发送报价', 'primary'),
(12002, NULL, '云启贸易', '续费提醒', '客户希望先确认本季度使用数据。', '李四', '2026-04-27 16:30:00', '准备使用报告', 'warning'),
(12003, NULL, '远山教育', '满意度回访', '服务反馈良好，推荐试用营销自动化插件。', '王五', '2026-04-26 09:40:00', '', 'success');

USE crm_sales;

CREATE TABLE IF NOT EXISTS crm_product (
    id BIGINT NOT NULL COMMENT '主键ID',
    code VARCHAR(80) NOT NULL COMMENT '产品编码',
    name VARCHAR(200) NOT NULL COMMENT '产品名称',
    category VARCHAR(100) DEFAULT NULL COMMENT '产品分类',
    price DECIMAL(15,2) DEFAULT 0.00 COMMENT '标准价',
    stock INT DEFAULT 0 COMMENT '库存',
    status TINYINT DEFAULT 1 COMMENT '状态 (0-下架, 1-上架)',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_code (code),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';

CREATE TABLE IF NOT EXISTS crm_sales_order (
    id BIGINT NOT NULL COMMENT '主键ID',
    order_no VARCHAR(80) NOT NULL COMMENT '订单编号',
    contact_id BIGINT DEFAULT NULL COMMENT '客户ID',
    customer_name VARCHAR(200) DEFAULT NULL COMMENT '客户名称',
    product_id BIGINT DEFAULT NULL COMMENT '产品ID',
    product_name VARCHAR(200) DEFAULT NULL COMMENT '产品名称',
    amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '合同金额',
    owner_id BIGINT DEFAULT NULL COMMENT '销售ID',
    owner_name VARCHAR(100) DEFAULT NULL COMMENT '销售姓名',
    sign_date DATE DEFAULT NULL COMMENT '签约日期',
    status TINYINT DEFAULT 1 COMMENT '状态 (1-已签约, 2-回款中, 3-已完成)',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_order_no (order_no),
    INDEX idx_contact_id (contact_id),
    INDEX idx_sign_date (sign_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单表';

INSERT IGNORE INTO crm_product (id, code, name, category, price, stock, status, remark) VALUES
(20001, 'CRM-SaaS-001', 'CRM 标准版', '软件订阅', 9800.00, 999, 1, '标准 CRM 套餐'),
(20002, 'CRM-AUTO-002', '营销自动化插件', '增购模块', 3600.00, 999, 1, '营销自动化增强包'),
(20003, 'CRM-SVC-003', '数据迁移服务包', '交付服务', 5200.00, 20, 0, '历史数据迁移服务');

INSERT IGNORE INTO crm_sales_order (id, order_no, contact_id, customer_name, product_id, product_name, amount, owner_id, owner_name, sign_date, status, remark) VALUES
(21001, 'SO202604001', 10001, '星河科技有限公司', 20001, 'CRM 标准版', 98000.00, 1, '张三', '2026-04-12', 1, '首年合同'),
(21002, 'SO202604002', NULL, '云启贸易', 20002, '营销自动化插件', 36000.00, 2, '李四', '2026-04-18', 2, '待回款'),
(21003, 'SO202604003', NULL, '远山教育', 20003, '客户成功服务包', 52000.00, 3, '王五', '2026-04-21', 3, '已完成');

USE crm_marketing;

CREATE TABLE IF NOT EXISTS crm_campaign_performance (
    id BIGINT NOT NULL COMMENT '主键ID',
    campaign_id BIGINT DEFAULT NULL COMMENT '活动ID',
    campaign_name VARCHAR(200) NOT NULL COMMENT '活动名称',
    channel VARCHAR(50) DEFAULT NULL COMMENT '触达渠道',
    sent_count INT DEFAULT 0 COMMENT '触达数',
    open_count INT DEFAULT 0 COMMENT '打开数',
    click_count INT DEFAULT 0 COMMENT '点击数',
    conversion_count INT DEFAULT 0 COMMENT '转化数',
    cost_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '成本金额',
    revenue_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '收入金额',
    stat_month VARCHAR(20) DEFAULT NULL COMMENT '统计月份',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_campaign_id (campaign_id),
    INDEX idx_channel (channel),
    INDEX idx_stat_month (stat_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营销活动效果明细表';

ALTER TABLE crm_campaign_performance
    MODIFY COLUMN cost_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '成本金额',
    MODIFY COLUMN revenue_amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '收入金额';

INSERT IGNORE INTO crm_campaign (id, name, type, target_audience, content, status) VALUES
(30001, '春季续费唤醒', 1, 'VIP,重要客户,续费', '针对即将到期客户发送续费优惠方案。', 1),
(30002, '高价值客户升级包', 1, 'VIP,高价值', '推送高级版功能与行业案例。', 1);

INSERT IGNORE INTO crm_campaign_performance (id, campaign_id, campaign_name, channel, sent_count, open_count, click_count, conversion_count, cost_amount, revenue_amount, stat_month) VALUES
(31001, 30001, '春季续费唤醒', '邮件', 9820, 4124, 1571, 386, '12000', '57600', '6月'),
(31002, 30002, '高价值客户升级包', '人工跟进', 3260, 1793, 782, 168, '8000', '48800', '6月');

USE crm_service;

CREATE TABLE IF NOT EXISTS crm_knowledge_article (
    id BIGINT NOT NULL COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    category VARCHAR(100) DEFAULT NULL COMMENT '分类',
    tags VARCHAR(500) DEFAULT NULL COMMENT '标签，逗号分隔',
    used_count INT DEFAULT 0 COMMENT '引用次数',
    solution TEXT DEFAULT NULL COMMENT '处理方案',
    steps TEXT DEFAULT NULL COMMENT '处理步骤，逗号分隔',
    status TINYINT DEFAULT 1 COMMENT '状态 (0-停用, 1-启用)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_category (category),
    INDEX idx_status (status),
    INDEX idx_used_count (used_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务知识库文章表';

CREATE TABLE IF NOT EXISTS crm_sla_rule (
    id BIGINT NOT NULL COMMENT '主键ID',
    title VARCHAR(100) NOT NULL COMMENT 'SLA名称',
    priority TINYINT DEFAULT 1 COMMENT '优先级',
    response_minutes INT DEFAULT 60 COMMENT '响应时限(分钟)',
    description VARCHAR(500) DEFAULT NULL COMMENT '说明',
    tone VARCHAR(30) DEFAULT 'normal' COMMENT '展示色调',
    status TINYINT DEFAULT 1 COMMENT '状态 (0-停用, 1-启用)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_priority (priority),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='SLA规则表';

CREATE TABLE IF NOT EXISTS crm_value_added_service (
    id BIGINT NOT NULL COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '服务名称',
    contact_id BIGINT DEFAULT NULL COMMENT '客户ID',
    customer_name VARCHAR(200) DEFAULT NULL COMMENT '客户名称',
    owner_name VARCHAR(100) DEFAULT NULL COMMENT '负责人',
    amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '服务金额',
    expire_date DATE DEFAULT NULL COMMENT '到期日期',
    status TINYINT DEFAULT 1 COMMENT '状态 (1-服务中, 2-即将到期, 3-已结束)',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_contact_id (contact_id),
    INDEX idx_expire_date (expire_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='增值服务表';

INSERT IGNORE INTO crm_ticket (id, ticket_no, title, contact_id, type, priority, status, assignee_id, content) VALUES
(50001, 'TK202604280001', '数据导入失败', 10001, 1, 3, 2, 1, '客户反馈导入模板校验不通过。');

INSERT IGNORE INTO crm_knowledge_article (id, title, category, tags, used_count, solution, steps, status) VALUES
(51001, '数据导入失败排查', '数据服务', '导入,模板,字段映射', 128, '先确认模板版本和必填字段，再检查手机号、邮箱等唯一字段是否重复。', '下载最新模板,校验必填字段,检查重复数据,重新发起导入任务', 1),
(51002, '客户无法收到营销邮件', '营销触达', '邮件,退订,送达率', 96, '检查客户是否退订、邮箱是否无效，以及活动发送域名是否通过校验。', '确认订阅状态,验证邮箱格式,检查发送记录,切换备用触达渠道', 1);

INSERT IGNORE INTO crm_sla_rule (id, title, priority, response_minutes, description, tone, status) VALUES
(51501, '紧急工单', 1, 15, '超时自动升级主管', 'critical', 1),
(51502, '高优先级', 2, 60, '需当天给出方案', 'high', 1),
(51503, '普通咨询', 3, 240, '工作时间内响应', 'normal', 1),
(51504, '建议反馈', 4, 1440, '进入需求池评估', 'low', 1);

INSERT IGNORE INTO crm_value_added_service (id, name, contact_id, customer_name, owner_name, amount, expire_date, status, remark) VALUES
(52001, '高级客户成功包', 10001, '星河科技有限公司', '张三', 28000.00, '2026-06-30', 1, '季度客户成功服务'),
(52002, '数据迁移服务', NULL, '云启贸易', '李四', 12000.00, '2026-05-12', 2, '即将到期需回访'),
(52003, '营销顾问服务', NULL, '远山教育', '王五', 36000.00, '2026-09-01', 1, '营销策略顾问');

USE crm_workflow;

CREATE TABLE IF NOT EXISTS crm_workflow_trace (
    id BIGINT NOT NULL COMMENT '主键ID',
    instance_id BIGINT NOT NULL COMMENT '工作流实例ID',
    step_order INT NOT NULL DEFAULT 1 COMMENT '步骤序号',
    step_name VARCHAR(200) NOT NULL COMMENT '步骤名称',
    step_status VARCHAR(30) DEFAULT 'processing' COMMENT '步骤状态',
    description VARCHAR(1000) DEFAULT NULL COMMENT '步骤描述',
    operator_name VARCHAR(100) DEFAULT NULL COMMENT '处理人',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_instance_id (instance_id),
    INDEX idx_step_order (instance_id, step_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流实例轨迹表';

INSERT IGNORE INTO crm_workflow (id, name, type, status, priority, description) VALUES
(70001, '线索自动分配', 1, 1, 10, '高评分线索自动分配给销售人员');

INSERT IGNORE INTO crm_workflow_node (id, workflow_id, step_order, step_name, step_type, assignee_type, assignee_value, step_config) VALUES
(71001, 70001, 1, '判断线索评分', 4, 1, '1', '{"score":80}'),
(71002, 70001, 2, '分配销售人员', 3, 1, '1', '{"assigneeId":1}');

INSERT IGNORE INTO crm_workflow_instance (id, workflow_id, workflow_name, trigger_type, status, current_node_order, current_node_name, result, start_params) VALUES
(72001, 70001, '线索自动分配', 1, 1, 2, '分配销售人员', '{"action":"线索分配","triggered":true}', '{"leadId":1}');

INSERT IGNORE INTO crm_workflow_trace (id, instance_id, step_order, step_name, step_status, description, operator_name) VALUES
(73001, 72001, 1, '触发流程', 'finished', '创建流程实例并写入上下文', '系统'),
(73002, 72001, 2, '分配销售人员', 'processing', '当前待处理节点', '系统');

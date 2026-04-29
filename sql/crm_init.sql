-- =============================================
-- CRM 系统数据库初始化脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS crm_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS crm_customer DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS crm_sales DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS crm_marketing DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS crm_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS crm_leads DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS crm_workflow DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- =============================================
-- 1. 认证数据库 (crm_auth)
-- =============================================
USE crm_auth;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码 (BCrypt加密)',
    real_name VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (0-禁用, 1-正常)',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 初始化管理员 (密码: admin123 的 BCrypt 哈希)
INSERT INTO sys_user (id, username, password, real_name, status) VALUES
(1, 'admin', '$2a$10$PayyMY6ynJhwdreCSDi0Ae2vPq9RjOyCucT7wggOxjKXmHuvV4VQC', '系统管理员', 1);

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT NOT NULL COMMENT '主键ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL COMMENT '角色标识',
    role_sort INT NOT NULL DEFAULT 0 COMMENT '排序',
    data_scope TINYINT NOT NULL DEFAULT 1 COMMENT '数据范围 (1-全部, 2-自定义, 3-本部门, 4-本部门及以下, 5-仅本人)',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (0-禁用, 1-正常)',
    description VARCHAR(200) DEFAULT NULL COMMENT '描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_role_key (role_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单表
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGINT NOT NULL COMMENT '主键ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    menu_type CHAR(1) NOT NULL DEFAULT 'M' COMMENT '菜单类型 (M-目录, C-菜单, F-按钮)',
    path VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
    component VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
    perms VARCHAR(200) DEFAULT NULL COMMENT '权限标识',
    icon VARCHAR(100) DEFAULT NULL COMMENT '图标',
    order_num INT NOT NULL DEFAULT 0 COMMENT '排序',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (0-隐藏, 1-显示)',
    visible TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见 (0-隐藏, 1-可见)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 部门表
CREATE TABLE IF NOT EXISTS sys_dept (
    id BIGINT NOT NULL COMMENT '主键ID',
    parent_id BIGINT NOT NULL DEFAULT 0 COMMENT '父部门ID',
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    order_num INT NOT NULL DEFAULT 0 COMMENT '排序',
    leader VARCHAR(50) DEFAULT NULL COMMENT '负责人',
    phone VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 (0-禁用, 1-正常)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 用户-角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色-菜单关联表
CREATE TABLE IF NOT EXISTS sys_role_menu (
    id BIGINT NOT NULL COMMENT '主键ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_role_menu (role_id, menu_id),
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_oper_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(100) DEFAULT NULL COMMENT '操作模块',
    oper_type VARCHAR(50) DEFAULT NULL COMMENT '操作类型 (INSERT/UPDATE/DELETE/OTHER)',
    oper_name VARCHAR(50) DEFAULT NULL COMMENT '操作人员',
    request_url VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
    request_method VARCHAR(10) DEFAULT NULL COMMENT '请求方式',
    request_params TEXT DEFAULT NULL COMMENT '请求参数',
    json_result TEXT DEFAULT NULL COMMENT '返回结果',
    status TINYINT DEFAULT 0 COMMENT '操作状态 (0-失败, 1-成功)',
    error_msg TEXT DEFAULT NULL COMMENT '错误消息',
    cost_time BIGINT DEFAULT 0 COMMENT '耗时(毫秒)',
    oper_ip VARCHAR(50) DEFAULT NULL COMMENT '操作IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    INDEX idx_oper_name (oper_name),
    INDEX idx_create_time (create_time),
    INDEX idx_title (title)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 通知记录表
CREATE TABLE IF NOT EXISTS sys_notification (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    message TEXT DEFAULT NULL COMMENT '通知内容',
    type VARCHAR(20) DEFAULT 'info' COMMENT '通知类型 (success/warning/info/error)',
    sender VARCHAR(50) DEFAULT NULL COMMENT '发送者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知记录表';

-- 初始化角色
INSERT INTO sys_role (id, role_name, role_key, role_sort, data_scope, status, description) VALUES
(1, '超级管理员', 'admin', 1, 1, 1, '拥有所有权限'),
(2, '销售经理',    'sales_mgr',  2, 3, 1, '管理销售团队'),
(3, '普通用户',    'user',       3, 5, 1, '普通用户');

-- 初始化菜单（目录+菜单）
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, order_num, status) VALUES
(1,  0, '系统管理', 'M', 'system', NULL, NULL, 'Setting', 99, 1),
(2,  1, '用户管理', 'C', 'user',  'system/UserList',  'system:user:list',  'User',  1, 1),
(3,  1, '角色管理', 'C', 'role',  'system/RoleList',  'system:role:list',  'Avatar', 2, 1),
(4,  1, '菜单管理', 'C', 'menu',  'system/MenuList',  'system:menu:list',  'Menu',  3, 1),
(5,  1, '部门管理', 'C', 'dept',  'system/DeptList',  'system:dept:list',  'OfficeBuilding', 4, 1),
(6,  2, '用户新增', 'F', NULL, NULL, 'system:user:add',  NULL, 1, 1),
(7,  2, '用户编辑', 'F', NULL, NULL, 'system:user:edit', NULL, 2, 1),
(8,  2, '用户删除', 'F', NULL, NULL, 'system:user:del',  NULL, 3, 1),
(9,  3, '角色新增', 'F', NULL, NULL, 'system:role:add',  NULL, 1, 1),
(10, 3, '角色编辑', 'F', NULL, NULL, 'system:role:edit', NULL, 2, 1),
(11, 3, '角色删除', 'F', NULL, NULL, 'system:role:del',  NULL, 3, 1),
(12, 4, '菜单新增', 'F', NULL, NULL, 'system:menu:add',  NULL, 1, 1),
(13, 4, '菜单编辑', 'F', NULL, NULL, 'system:menu:edit', NULL, 2, 1),
(14, 4, '菜单删除', 'F', NULL, NULL, 'system:menu:del',  NULL, 3, 1),
(15, 5, '部门新增', 'F', NULL, NULL, 'system:dept:add',  NULL, 1, 1),
(16, 5, '部门编辑', 'F', NULL, NULL, 'system:dept:edit', NULL, 2, 1),
(17, 5, '部门删除', 'F', NULL, NULL, 'system:dept:del',  NULL, 3, 1);

-- 初始化管理员-超级管理员关联
INSERT INTO sys_user_role (id, user_id, role_id) VALUES (1, 1, 1);

-- 初始化超级管理员拥有所有菜单权限
INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES
(1, 1, 1),(2, 1, 2),(3, 1, 3),(4, 1, 4),(5, 1, 5),
(6, 1, 6),(7, 1, 7),(8, 1, 8),(9, 1, 9),(10, 1, 10),
(11, 1, 11),(12, 1, 12),(13, 1, 13),(14, 1, 14),(15, 1, 15),(16, 1, 16),(17, 1, 17);

-- 初始化业务模块菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, order_num, status) VALUES
(18, 0, '仪表盘',     'C', 'dashboard', NULL, NULL, 'DataBoard', 1, 1),
(19, 0, '客户管理',   'C', 'customer',  NULL, 'customer:list',     'User',        2, 1),
(20, 0, '商机管理',   'C', 'sales',     NULL, 'sales:list',        'TrendCharts', 3, 1),
(21, 0, '营销自动化', 'C', 'marketing', NULL, 'marketing:list',    'Promotion',   4, 1),
(22, 0, '客户服务',   'C', 'service',   NULL, 'service:list',      'Service',     5, 1),
(23, 0, '线索管理',   'C', 'leads',     NULL, 'leads:list',        'Search',      6, 1),
(24, 0, '工作流自动化','C','workflow',   NULL, 'workflow:list',     'SetUp',       7, 1),
(100, 0, '产品管理', 'C', 'product', NULL, 'product:list', 'Box', 8, 1),
(101, 0, '增值服务', 'C', 'value-service', NULL, 'value-service:list', 'Service', 9, 1),
(102, 0, '销售管理', 'C', 'sales-management', NULL, 'sales-management:list', 'Money', 10, 1),
(103, 0, '跟进记录', 'C', 'followup', NULL, 'followup:list', 'Document', 11, 1),
(104, 0, '数据统计', 'C', 'statistics', NULL, 'statistics:list', 'Histogram', 12, 1);

-- 初始化业务模块按钮级权限
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, order_num, status) VALUES
(27, 19, '客户新增', 'F', NULL, NULL, 'customer:add', NULL, 1, 1),
(28, 19, '客户编辑', 'F', NULL, NULL, 'customer:edit', NULL, 2, 1),
(29, 19, '客户删除', 'F', NULL, NULL, 'customer:del', NULL, 3, 1),
(30, 20, '商机新增', 'F', NULL, NULL, 'sales:add', NULL, 1, 1),
(31, 20, '商机编辑', 'F', NULL, NULL, 'sales:edit', NULL, 2, 1),
(32, 20, '商机删除', 'F', NULL, NULL, 'sales:del', NULL, 3, 1),
(33, 21, '活动新增', 'F', NULL, NULL, 'marketing:add', NULL, 1, 1),
(34, 21, '活动编辑', 'F', NULL, NULL, 'marketing:edit', NULL, 2, 1),
(35, 21, '活动删除', 'F', NULL, NULL, 'marketing:del', NULL, 3, 1),
(36, 22, '工单新增', 'F', NULL, NULL, 'service:add', NULL, 1, 1),
(37, 22, '工单编辑', 'F', NULL, NULL, 'service:edit', NULL, 2, 1),
(38, 22, '工单删除', 'F', NULL, NULL, 'service:del', NULL, 3, 1),
(39, 23, '线索新增', 'F', NULL, NULL, 'leads:add', NULL, 1, 1),
(40, 23, '线索编辑', 'F', NULL, NULL, 'leads:edit', NULL, 2, 1),
(41, 23, '线索删除', 'F', NULL, NULL, 'leads:del', NULL, 3, 1),
(42, 24, '工作流新增','F', NULL, NULL, 'workflow:add', NULL, 1, 1),
(43, 24, '工作流编辑','F', NULL, NULL, 'workflow:edit', NULL, 2, 1),
(44, 24, '工作流删除','F', NULL, NULL, 'workflow:del', NULL, 3, 1),
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

-- 初始化系统管理子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, order_num, status) VALUES
(25, 1, '操作日志',  'C', 'oper-log', 'system/OperLog',  'system:operlog:list',  'Document',     5, 1),
(26, 1, '通知管理',  'C', 'notif',    'system/NotifManage', 'system:notif:send',   'Bell',         6, 1),
(117, 1, 'Sentinel 控制台', 'C', 'sentinel', 'system/SentinelConsole', 'system:sentinel:view', 'Monitor', 7, 1);

-- 初始化超级管理员拥有所有菜单权限（含业务模块）
INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES
(18, 1, 18),(19, 1, 19),(20, 1, 20),(21, 1, 21),
(22, 1, 22),(23, 1, 23),(24, 1, 24),
(25, 1, 25),(26, 1, 26),(117, 1, 117),
(27, 1, 27),(28, 1, 28),(29, 1, 29), (30, 1, 30),(31, 1, 31),(32, 1, 32),
(33, 1, 33),(34, 1, 34),(35, 1, 35), (36, 1, 36),(37, 1, 37),(38, 1, 38),
(39, 1, 39),(40, 1, 40),(41, 1, 41), (42, 1, 42),(43, 1, 43),(44, 1, 44),
(100, 1, 100),(101, 1, 101),(102, 1, 102),(103, 1, 103),(104, 1, 104),
(105, 1, 105),(106, 1, 106),(107, 1, 107),(108, 1, 108),(109, 1, 109),
(110, 1, 110),(111, 1, 111),(112, 1, 112),(113, 1, 113),(114, 1, 114);

-- 初始化部门
INSERT INTO sys_dept (id, parent_id, dept_name, order_num, leader, status) VALUES
(1, 0, '总公司',    0, 'Admin', 1),
(2, 1, '销售部',    1, '张三',  1),
(3, 1, '市场部',    2, '李四',  1),
(4, 1, '技术部',    3, '王五',  1);

-- =============================================
-- 2. 客户管理数据库 (crm_customer)
-- =============================================
USE crm_customer;

CREATE TABLE IF NOT EXISTS crm_contact (
    id BIGINT NOT NULL COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    company VARCHAR(200) DEFAULT NULL COMMENT '公司名称',
    position VARCHAR(100) DEFAULT NULL COMMENT '职位',
    source TINYINT DEFAULT 1 COMMENT '客户来源 (1-网站, 2-推荐, 3-展会, 4-其他)',
    level TINYINT DEFAULT 1 COMMENT '客户等级 (1-普通, 2-重要, 3-VIP)',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 (0-未删除, 1-已删除)',
    PRIMARY KEY (id),
    INDEX idx_phone (phone),
    INDEX idx_name (name),
    INDEX idx_company (company)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客户联系人表';

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

INSERT INTO crm_contact (id, name, phone, email, company, position, source, level, remark) VALUES
(10001, '王明', '13800000001', 'wangming@example.com', '星河科技有限公司', '采购负责人', 1, 3, '重点跟进客户');

INSERT INTO crm_customer_interaction (id, contact_id, interaction_type, title, description, timeline_type) VALUES
(11001, 10001, 'call', '销售电话跟进', '确认采购预算，客户希望下周收到正式报价。', 'primary'),
(11002, 10001, 'email', '营销邮件打开', '打开产品升级邮件并点击案例链接。', 'success'),
(11003, 10001, 'ticket', '服务咨询记录', '咨询数据导入规则，客服已给出处理方案。', 'warning');

INSERT INTO crm_followup_record (id, contact_id, customer_name, title, content, owner_name, follow_time, next_action, timeline_type) VALUES
(12001, 10001, '星河科技有限公司', '报价沟通', '客户认可方案，需补充实施周期和付款节点。', '张三', '2026-04-28 10:20:00', '明天发送报价', 'primary'),
(12002, NULL, '云启贸易', '续费提醒', '客户希望先确认本季度使用数据。', '李四', '2026-04-27 16:30:00', '准备使用报告', 'warning'),
(12003, NULL, '远山教育', '满意度回访', '服务反馈良好，推荐试用营销自动化插件。', '王五', '2026-04-26 09:40:00', '', 'success');

-- =============================================
-- 3. 销售自动化数据库 (crm_sales)
-- =============================================
USE crm_sales;

CREATE TABLE IF NOT EXISTS crm_opportunity (
    id BIGINT NOT NULL COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '商机名称',
    contact_id BIGINT DEFAULT NULL COMMENT '关联客户ID',
    amount DECIMAL(15,2) DEFAULT 0.00 COMMENT '预计成交金额',
    stage TINYINT DEFAULT 1 COMMENT '销售阶段 (1-初步接触, 2-需求分析, 3-方案报价, 4-谈判, 5-赢单, 6-输单)',
    expected_close_date DATE DEFAULT NULL COMMENT '预计成交日期',
    owner_id BIGINT DEFAULT NULL COMMENT '负责人ID',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_stage (stage),
    INDEX idx_owner_id (owner_id),
    INDEX idx_expected_close_date (expected_close_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商机表';

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

INSERT INTO crm_product (id, code, name, category, price, stock, status, remark) VALUES
(20001, 'CRM-SaaS-001', 'CRM 标准版', '软件订阅', 9800.00, 999, 1, '标准 CRM 套餐'),
(20002, 'CRM-AUTO-002', '营销自动化插件', '增购模块', 3600.00, 999, 1, '营销自动化增强包'),
(20003, 'CRM-SVC-003', '数据迁移服务包', '交付服务', 5200.00, 20, 0, '历史数据迁移服务');

INSERT INTO crm_sales_order (id, order_no, contact_id, customer_name, product_id, product_name, amount, owner_id, owner_name, sign_date, status, remark) VALUES
(21001, 'SO202604001', 10001, '星河科技有限公司', 20001, 'CRM 标准版', 98000.00, 1, '张三', '2026-04-12', 1, '首年合同'),
(21002, 'SO202604002', NULL, '云启贸易', 20002, '营销自动化插件', 36000.00, 2, '李四', '2026-04-18', 2, '待回款'),
(21003, 'SO202604003', NULL, '远山教育', 20003, '客户成功服务包', 52000.00, 3, '王五', '2026-04-21', 3, '已完成');

-- =============================================
-- 4. 营销自动化数据库 (crm_marketing)
-- =============================================
USE crm_marketing;

CREATE TABLE IF NOT EXISTS crm_campaign (
    id BIGINT NOT NULL COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '活动名称',
    type TINYINT DEFAULT 1 COMMENT '活动类型 (1-邮件, 2-短信, 3-推送, 4-其他)',
    target_audience VARCHAR(500) DEFAULT NULL COMMENT '目标客户群体',
    content TEXT DEFAULT NULL COMMENT '活动内容',
    status TINYINT DEFAULT 0 COMMENT '状态 (0-草稿, 1-进行中, 2-已完成, 3-已取消)',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_type (type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='营销活动表';

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

INSERT INTO crm_campaign (id, name, type, target_audience, content, status) VALUES
(30001, '春季续费唤醒', 1, 'VIP,重要客户,续费', '针对即将到期客户发送续费优惠方案。', 1),
(30002, '高价值客户升级包', 1, 'VIP,高价值', '推送高级版功能与行业案例。', 1);

INSERT INTO crm_campaign_performance (id, campaign_id, campaign_name, channel, sent_count, open_count, click_count, conversion_count, cost_amount, revenue_amount, stat_month) VALUES
(31001, 30001, '春季续费唤醒', '邮件', 9820, 4124, 1571, 386, '12000', '57600', '6月'),
(31002, 30002, '高价值客户升级包', '人工跟进', 3260, 1793, 782, 168, '8000', '48800', '6月');

-- =============================================
-- 5. 客户服务数据库 (crm_service)
-- =============================================
USE crm_service;

CREATE TABLE IF NOT EXISTS crm_ticket (
    id BIGINT NOT NULL COMMENT '主键ID',
    ticket_no VARCHAR(50) NOT NULL COMMENT '工单编号',
    title VARCHAR(200) NOT NULL COMMENT '工单标题',
    contact_id BIGINT DEFAULT NULL COMMENT '关联客户ID',
    type TINYINT DEFAULT 1 COMMENT '工单类型 (1-咨询, 2-投诉, 3-建议, 4-其他)',
    priority TINYINT DEFAULT 1 COMMENT '优先级 (1-低, 2-中, 3-高, 4-紧急)',
    status TINYINT DEFAULT 1 COMMENT '状态 (1-待处理, 2-处理中, 3-已解决, 4-已关闭)',
    assignee_id BIGINT DEFAULT NULL COMMENT '处理人ID',
    content TEXT DEFAULT NULL COMMENT '工单内容',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_ticket_no (ticket_no),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_assignee_id (assignee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务工单表';

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

INSERT INTO crm_value_added_service (id, name, contact_id, customer_name, owner_name, amount, expire_date, status, remark) VALUES
(52001, '高级客户成功包', 10001, '星河科技有限公司', '张三', 28000.00, '2026-06-30', 1, '季度客户成功服务'),
(52002, '数据迁移服务', NULL, '云启贸易', '李四', 12000.00, '2026-05-12', 2, '即将到期需回访'),
(52003, '营销顾问服务', NULL, '远山教育', '王五', 36000.00, '2026-09-01', 1, '营销策略顾问');

INSERT INTO crm_ticket (id, ticket_no, title, contact_id, type, priority, status, assignee_id, content) VALUES
(50001, 'TK202604280001', '数据导入失败', 10001, 1, 3, 2, 1, '客户反馈导入模板校验不通过。');

INSERT INTO crm_knowledge_article (id, title, category, tags, used_count, solution, steps, status) VALUES
(51001, '数据导入失败排查', '数据服务', '导入,模板,字段映射', 128, '先确认模板版本和必填字段，再检查手机号、邮箱等唯一字段是否重复。', '下载最新模板,校验必填字段,检查重复数据,重新发起导入任务', 1),
(51002, '客户无法收到营销邮件', '营销触达', '邮件,退订,送达率', 96, '检查客户是否退订、邮箱是否无效，以及活动发送域名是否通过校验。', '确认订阅状态,验证邮箱格式,检查发送记录,切换备用触达渠道', 1);

INSERT INTO crm_sla_rule (id, title, priority, response_minutes, description, tone, status) VALUES
(51501, '紧急工单', 1, 15, '超时自动升级主管', 'critical', 1),
(51502, '高优先级', 2, 60, '需当天给出方案', 'high', 1),
(51503, '普通咨询', 3, 240, '工作时间内响应', 'normal', 1),
(51504, '建议反馈', 4, 1440, '进入需求池评估', 'low', 1);

-- =============================================
-- 6. 线索管理数据库 (crm_leads)
-- =============================================
USE crm_leads;

CREATE TABLE IF NOT EXISTS crm_lead (
    id BIGINT NOT NULL COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '线索名称',
    contact_info VARCHAR(200) DEFAULT NULL COMMENT '联系方式',
    source TINYINT DEFAULT 1 COMMENT '线索来源 (1-官网, 2-广告, 3-推荐, 4-社交媒体, 5-其他)',
    score INT DEFAULT 0 COMMENT '线索评分 (0-100)',
    status TINYINT DEFAULT 1 COMMENT '状态 (1-新建, 2-已分配, 3-已转化, 4-已关闭)',
    assignee_id BIGINT DEFAULT NULL COMMENT '分配人ID',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_source (source),
    INDEX idx_score (score),
    INDEX idx_status (status),
    INDEX idx_assignee_id (assignee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售线索表';

-- =============================================
-- 7. 工作流自动化数据库 (crm_workflow)
-- =============================================
USE crm_workflow;

CREATE TABLE IF NOT EXISTS crm_workflow (
    id BIGINT NOT NULL COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '工作流名称',
    type TINYINT DEFAULT 1 COMMENT '工作流类型 (1-线索分配, 2-合同审批, 3-工单流转, 4-其他)',
    trigger_condition TEXT DEFAULT NULL COMMENT '触发条件 (JSON格式)',
    action TEXT DEFAULT NULL COMMENT '执行动作 (JSON格式)',
    status TINYINT DEFAULT 1 COMMENT '状态 (0-禁用, 1-启用)',
    priority INT DEFAULT 0 COMMENT '优先级',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_type (type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流表';

CREATE TABLE IF NOT EXISTS crm_workflow_node (
    id BIGINT NOT NULL COMMENT '主键ID',
    workflow_id BIGINT NOT NULL COMMENT '所属工作流ID',
    step_order INT NOT NULL DEFAULT 1 COMMENT '步骤序号',
    step_name VARCHAR(200) NOT NULL COMMENT '步骤名称',
    step_type TINYINT DEFAULT 1 COMMENT '步骤类型 (1-审批, 2-通知, 3-自动操作, 4-条件分支)',
    assignee_type TINYINT DEFAULT 1 COMMENT '处理人类型 (1-指定人, 2-角色)',
    assignee_value VARCHAR(200) DEFAULT NULL COMMENT '处理人值',
    step_config TEXT DEFAULT NULL COMMENT '步骤配置 (JSON格式)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_workflow_id (workflow_id),
    INDEX idx_step_order (workflow_id, step_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流节点表';

CREATE TABLE IF NOT EXISTS crm_workflow_instance (
    id BIGINT NOT NULL COMMENT '主键ID',
    workflow_id BIGINT NOT NULL COMMENT '关联工作流ID',
    workflow_name VARCHAR(200) DEFAULT NULL COMMENT '工作流名称',
    trigger_type TINYINT DEFAULT 1 COMMENT '触发类型 (1-手动, 2-自动)',
    status TINYINT DEFAULT 1 COMMENT '执行状态 (1-进行中, 2-已完成, 3-已取消, 4-失败)',
    current_node_order INT DEFAULT 1 COMMENT '当前节点序号',
    current_node_name VARCHAR(200) DEFAULT NULL COMMENT '当前节点名称',
    result TEXT DEFAULT NULL COMMENT '执行结果 (JSON格式)',
    start_params TEXT DEFAULT NULL COMMENT '启动参数 (JSON格式)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT DEFAULT NULL COMMENT '创建人',
    update_by BIGINT DEFAULT NULL COMMENT '更新人',
    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    INDEX idx_workflow_id (workflow_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流执行实例表';

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

INSERT INTO crm_workflow (id, name, type, status, priority, description) VALUES
(70001, '线索自动分配', 1, 1, 10, '高评分线索自动分配给销售人员');

INSERT INTO crm_workflow_node (id, workflow_id, step_order, step_name, step_type, assignee_type, assignee_value, step_config) VALUES
(71001, 70001, 1, '判断线索评分', 4, 1, '1', '{"score":80}'),
(71002, 70001, 2, '分配销售人员', 3, 1, '1', '{"assigneeId":1}');

INSERT INTO crm_workflow_instance (id, workflow_id, workflow_name, trigger_type, status, current_node_order, current_node_name, result, start_params) VALUES
(72001, 70001, '线索自动分配', 1, 1, 2, '分配销售人员', '{"action":"线索分配","triggered":true}', '{"leadId":1}');

INSERT INTO crm_workflow_trace (id, instance_id, step_order, step_name, step_status, description, operator_name) VALUES
(73001, 72001, 1, '触发流程', 'finished', '创建流程实例并写入上下文', '系统'),
(73002, 72001, 2, '分配销售人员', 'processing', '当前待处理节点', '系统');

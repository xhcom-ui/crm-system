-- =============================================
-- CRM 二级菜单升级脚本
-- 适用于已经执行过 crm_init.sql 及业务增量脚本的环境
-- =============================================

USE crm_auth;

INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, order_num, status, visible)
VALUES
(300, 0, '工作台', 'M', 'workspace', NULL, NULL, 'DataBoard', 1, 1, 1),
(301, 0, '客户运营', 'M', 'customer-ops', NULL, NULL, 'User', 2, 1, 1),
(302, 0, '销售交易', 'M', 'sales-ops', NULL, NULL, 'TrendCharts', 3, 1, 1),
(303, 0, '产品服务', 'M', 'product-service', NULL, NULL, 'Box', 4, 1, 1),
(304, 0, '营销增长', 'M', 'marketing-growth', NULL, NULL, 'Promotion', 5, 1, 1),
(305, 0, '自动化', 'M', 'automation', NULL, NULL, 'SetUp', 6, 1, 1),
(115, 304, '营销效果报表', 'C', '/marketing/report', NULL, 'marketing:list', 'Histogram', 2, 1, 1),
(116, 305, '审批实例追踪', 'C', '/workflow/instances', NULL, 'workflow:list', 'Connection', 2, 1, 1),
(117, 1, 'Sentinel 控制台', 'C', 'sentinel', 'system/SentinelConsole', 'system:sentinel:view', 'Monitor', 7, 1, 1);

UPDATE sys_menu SET parent_id = 300, path = '/dashboard', order_num = 1 WHERE id = 18;
UPDATE sys_menu SET parent_id = 300, path = '/statistics', order_num = 2 WHERE id = 104;

UPDATE sys_menu SET parent_id = 301, path = '/customer', order_num = 1 WHERE id = 19;
UPDATE sys_menu SET parent_id = 301, path = '/followup', order_num = 2 WHERE id = 103;
UPDATE sys_menu SET parent_id = 301, path = '/fat-management', order_num = 3 WHERE id = 201;

UPDATE sys_menu SET parent_id = 302, path = '/leads', order_num = 1 WHERE id = 23;
UPDATE sys_menu SET parent_id = 302, path = '/sales', order_num = 2 WHERE id = 20;
UPDATE sys_menu SET parent_id = 302, path = '/sales-management', order_num = 3 WHERE id = 102;
UPDATE sys_menu SET parent_id = 302, path = '/payment', order_num = 4 WHERE id = 202;

UPDATE sys_menu SET parent_id = 303, path = '/product', order_num = 1 WHERE id = 100;
UPDATE sys_menu SET parent_id = 303, path = '/value-service', order_num = 2 WHERE id = 101;
UPDATE sys_menu SET parent_id = 303, path = '/service', menu_name = '客户服务', icon = 'Headset', order_num = 3 WHERE id = 22;
UPDATE sys_menu SET parent_id = 303, path = '/inventory', order_num = 4 WHERE id = 203;

UPDATE sys_menu SET parent_id = 304, path = '/marketing', menu_name = '营销活动', order_num = 1 WHERE id = 21;
UPDATE sys_menu SET parent_id = 305, path = '/workflow', order_num = 1 WHERE id = 24;
UPDATE sys_menu SET parent_id = 0, order_num = 99 WHERE id = 1;

INSERT IGNORE INTO sys_role_menu (id, role_id, menu_id)
VALUES
(300, 1, 300),(301, 1, 301),(302, 1, 302),(303, 1, 303),(304, 1, 304),(305, 1, 305),
(306, 1, 115),(307, 1, 116),(308, 1, 117);

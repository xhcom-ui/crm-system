USE crm_auth;

INSERT IGNORE INTO sys_menu
(id, parent_id, menu_name, menu_type, path, component, perms, icon, order_num, status, visible)
VALUES
(117, 1, 'Sentinel 控制台', 'C', 'sentinel', 'system/SentinelConsole', 'system:sentinel:view', 'Monitor', 7, 1, 1);

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT COALESCE(MAX(id), 0) + 1, 1, 117
FROM sys_role_menu
HAVING NOT EXISTS (
    SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = 117
);

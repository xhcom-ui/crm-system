USE crm_auth;

UPDATE sys_menu
SET menu_name = '客户信息管理',
    icon = 'DataLine',
    perms = 'fat-management:list'
WHERE id = 201;

UPDATE sys_menu
SET menu_name = '客户信息新增'
WHERE id = 204;

UPDATE sys_menu
SET menu_name = '客户信息编辑'
WHERE id = 205;

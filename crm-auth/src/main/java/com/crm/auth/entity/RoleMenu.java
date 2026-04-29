package com.crm.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色-菜单关联实体
 */
@Data
@TableName("sys_role_menu")
public class RoleMenu {

    /** 主键ID */
    private Long id;

    /** 角色ID */
    private Long roleId;

    /** 菜单ID */
    private Long menuId;
}

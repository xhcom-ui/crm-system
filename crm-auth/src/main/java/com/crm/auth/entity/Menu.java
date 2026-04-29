package com.crm.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 菜单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class Menu extends BaseEntity {

    /** 父菜单ID */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 菜单类型 (M-目录, C-菜单, F-按钮) */
    private String menuType;

    /** 路由路径 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 权限标识 */
    private String perms;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer orderNum;

    /** 状态 (0-隐藏, 1-显示) */
    private Integer status;

    /** 是否可见 (0-隐藏, 1-可见) */
    private Integer visible;

    /** 子菜单列表 (非数据库字段) */
    @TableField(exist = false)
    private List<Menu> children;
}

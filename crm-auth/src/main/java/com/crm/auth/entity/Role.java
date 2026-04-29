package com.crm.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class Role extends BaseEntity {

    /** 角色名称 */
    private String roleName;

    /** 角色标识 */
    private String roleKey;

    /** 排序 */
    private Integer roleSort;

    /** 数据范围 (1-全部, 2-自定义, 3-本部门, 4-本部门及以下, 5-仅本人) */
    private Integer dataScope;

    /** 状态 (0-禁用, 1-正常) */
    private Integer status;

    /** 描述 */
    private String description;
}

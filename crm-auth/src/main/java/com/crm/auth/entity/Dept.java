package com.crm.auth.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.crm.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 部门实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_dept")
public class Dept extends BaseEntity {

    /** 父部门ID */
    private Long parentId;

    /** 部门名称 */
    private String deptName;

    /** 排序 */
    private Integer orderNum;

    /** 负责人 */
    private String leader;

    /** 联系电话 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 状态 (0-禁用, 1-正常) */
    private Integer status;

    /** 子部门列表 (非数据库字段) */
    @TableField(exist = false)
    private List<Dept> children;
}

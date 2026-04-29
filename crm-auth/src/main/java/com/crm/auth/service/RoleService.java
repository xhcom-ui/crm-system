package com.crm.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.crm.auth.entity.Role;

import java.util.List;

/**
 * 角色 Service
 */
public interface RoleService extends IService<Role> {

    /**
     * 根据用户ID查询角色列表
     */
    List<Role> getRolesByUserId(Long userId);

    /**
     * 查询所有启用的角色
     */
    List<Role> getEnabledRoles();
}

package com.crm.auth.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.crm.auth.entity.Role;
import com.crm.auth.entity.UserRole;
import com.crm.auth.mapper.RoleMapper;
import com.crm.auth.mapper.UserRoleMapper;
import com.crm.auth.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final UserRoleMapper userRoleMapper;

    @Override
    public List<Role> getRolesByUserId(Long userId) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId));
        if (userRoles.isEmpty()) return Collections.emptyList();
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        return this.listByIds(roleIds);
    }

    @Override
    public List<Role> getEnabledRoles() {
        return this.lambdaQuery().eq(Role::getStatus, 1).list();
    }
}

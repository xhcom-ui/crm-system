package com.crm.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.auth.entity.*;
import com.crm.auth.mapper.UserRoleMapper;
import com.crm.auth.service.RoleService;
import com.crm.auth.service.UserService;
import com.crm.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理 Controller
 */
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final RoleService roleService;
    private final UserRoleMapper userRoleMapper;

    /**
     * 用户列表（分页）
     */
    @GetMapping
    @SaCheckPermission("system:user:list")
    public Result<Page<User>> list(@RequestParam(defaultValue = "1") Integer current,
                                    @RequestParam(defaultValue = "10") Integer size) {
        Page<User> page = userService.page(new Page<>(current, size),
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime));
        // 脱敏
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(page);
    }

    /**
     * 用户详情
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 新增用户
     */
    @PostMapping
    @SaCheckPermission("system:user:add")
    public Result<Void> save(@RequestBody User user) {
        user.setPassword(cn.hutool.crypto.digest.BCrypt.hashpw(user.getPassword()));
        userService.save(user);
        return Result.success();
    }

    /**
     * 编辑用户
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:user:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(null); // 不修改密码
        } else {
            user.setPassword(cn.hutool.crypto.digest.BCrypt.hashpw(user.getPassword()));
        }
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:user:del")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        // 同时删除关联角色
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        return Result.success();
    }

    /**
     * 获取用户的角色ID列表
     */
    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoleIds(@PathVariable Long id) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        List<Long> roleIds = userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList());
        return Result.success(roleIds);
    }

    /**
     * 设置用户角色
     */
    @PutMapping("/{id}/roles")
    @SaCheckPermission("system:user:edit")
    public Result<Void> setUserRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> roleIds = body.get("roleIds");
        // 先删再增
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, id));
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole ur = new UserRole();
                ur.setUserId(id);
                ur.setRoleId(roleId);
                userRoleMapper.insert(ur);
            }
        }
        return Result.success();
    }
}

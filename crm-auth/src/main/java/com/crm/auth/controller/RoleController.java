package com.crm.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.auth.entity.*;
import com.crm.auth.mapper.RoleMenuMapper;
import com.crm.auth.mapper.UserRoleMapper;
import com.crm.auth.service.RoleService;
import com.crm.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 角色管理 Controller
 */
@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;

    /**
     * 角色列表（分页）
     */
    @GetMapping
    @SaCheckPermission("system:role:list")
    public Result<Page<Role>> list(@RequestParam(defaultValue = "1") Integer current,
                                    @RequestParam(defaultValue = "10") Integer size) {
        Page<Role> page = roleService.page(new Page<>(current, size),
                new LambdaQueryWrapper<Role>().orderByAsc(Role::getRoleSort));
        return Result.success(page);
    }

    /**
     * 全部角色（下拉框用）
     */
    @GetMapping("/all")
    public Result<List<Role>> all() {
        return Result.success(roleService.getEnabledRoles());
    }

    /**
     * 角色详情
     */
    @GetMapping("/{id}")
    public Result<Role> getById(@PathVariable Long id) {
        return Result.success(roleService.getById(id));
    }

    /**
     * 新增角色
     */
    @PostMapping
    @SaCheckPermission("system:role:add")
    public Result<Void> save(@RequestBody Role role) {
        roleService.save(role);
        return Result.success();
    }

    /**
     * 编辑角色
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:role:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        roleService.updateById(role);
        return Result.success();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:role:del")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.removeById(id);
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id));
        userRoleMapper.delete(new LambdaQueryWrapper<UserRole>().eq(UserRole::getRoleId, id));
        return Result.success();
    }

    /**
     * 获取角色的菜单ID列表
     */
    @GetMapping("/{id}/menus")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long id) {
        List<RoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id));
        List<Long> menuIds = roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toList());
        return Result.success(menuIds);
    }

    /**
     * 设置角色菜单权限
     */
    @PutMapping("/{id}/menus")
    @SaCheckPermission("system:role:edit")
    public Result<Void> setRoleMenus(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> menuIds = body.get("menuIds");
        roleMenuMapper.delete(new LambdaQueryWrapper<RoleMenu>().eq(RoleMenu::getRoleId, id));
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                RoleMenu rm = new RoleMenu();
                rm.setRoleId(id);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
        return Result.success();
    }
}

package com.crm.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.crm.auth.entity.Menu;
import com.crm.auth.entity.Role;
import com.crm.auth.entity.User;
import com.crm.auth.mapper.MenuMapper;
import com.crm.auth.service.MenuService;
import com.crm.auth.service.RoleService;
import com.crm.auth.service.UserService;
import com.crm.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 认证 Controller
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RoleService roleService;
    private final MenuService menuService;
    private final MenuMapper menuMapper;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String token = userService.login(username, password);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        return Result.success("登录成功", result);
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }

    /**
     * 获取当前用户信息（含角色与权限）
     */
    @GetMapping("/user-info")
    public Result<Map<String, Object>> getUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
        }

        // 获取角色列表
        List<Role> roles = roleService.getRolesByUserId(userId);
        List<String> roleKeys = roles.stream().map(Role::getRoleKey).collect(Collectors.toList());

        // 获取权限标识列表
        List<String> permissions = menuMapper.selectPermsByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("roles", roleKeys);
        result.put("permissions", permissions);
        return Result.success(result);
    }

    /**
     * 修改当前用户密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        Long userId = StpUtil.getLoginIdAsLong();
        userService.updatePassword(userId, oldPassword, newPassword);
        return Result.success();
    }

    /**
     * 获取当前用户的路由菜单（动态菜单树）
     */
    @GetMapping("/routes")
    public Result<List<Menu>> getRoutes() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Menu> menuTree = menuService.getUserMenuTree(userId);
        return Result.success(menuTree);
    }
}

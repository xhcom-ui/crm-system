package com.crm.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.crm.auth.entity.Menu;
import com.crm.auth.service.MenuService;
import com.crm.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理 Controller
 */
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    /**
     * 获取菜单树（管理用，含按钮）
     */
    @GetMapping("/tree")
    @SaCheckPermission("system:menu:list")
    public Result<List<Menu>> tree() {
        return Result.success(menuService.getAllMenuTree());
    }

    /**
     * 菜单详情
     */
    @GetMapping("/{id}")
    public Result<Menu> getById(@PathVariable Long id) {
        return Result.success(menuService.getById(id));
    }

    /**
     * 新增菜单
     */
    @PostMapping
    @SaCheckPermission("system:menu:add")
    public Result<Void> save(@RequestBody Menu menu) {
        menuService.save(menu);
        return Result.success();
    }

    /**
     * 编辑菜单
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:menu:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody Menu menu) {
        menu.setId(id);
        menuService.updateById(menu);
        return Result.success();
    }

    /**
     * 删除菜单
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:menu:del")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.removeById(id);
        return Result.success();
    }
}

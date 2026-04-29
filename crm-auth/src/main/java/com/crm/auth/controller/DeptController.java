package com.crm.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.crm.auth.entity.Dept;
import com.crm.auth.service.DeptService;
import com.crm.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理 Controller
 */
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    /**
     * 获取部门树
     */
    @GetMapping("/tree")
    @SaCheckPermission("system:dept:list")
    public Result<List<Dept>> tree() {
        return Result.success(deptService.buildDeptTree());
    }

    /**
     * 部门详情
     */
    @GetMapping("/{id}")
    public Result<Dept> getById(@PathVariable Long id) {
        return Result.success(deptService.getById(id));
    }

    /**
     * 新增部门
     */
    @PostMapping
    @SaCheckPermission("system:dept:add")
    public Result<Void> save(@RequestBody Dept dept) {
        deptService.save(dept);
        return Result.success();
    }

    /**
     * 编辑部门
     */
    @PutMapping("/{id}")
    @SaCheckPermission("system:dept:edit")
    public Result<Void> update(@PathVariable Long id, @RequestBody Dept dept) {
        dept.setId(id);
        deptService.updateById(dept);
        return Result.success();
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("system:dept:del")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.removeById(id);
        return Result.success();
    }
}

package com.crm.sales.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.log.OperationLog;
import com.crm.common.result.Result;
import com.crm.sales.entity.Product;
import com.crm.sales.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductMapper productMapper;

    @GetMapping("/page")
    @SaCheckPermission("product:list")
    public Result<IPage<Product>> page(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Product::getName, keyword).or().like(Product::getCode, keyword));
        }
        if (status != null) wrapper.eq(Product::getStatus, status);
        wrapper.orderByDesc(Product::getCreateTime);
        return Result.success(productMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @PostMapping
    @SaCheckPermission("product:add")
    @OperationLog(title = "新增产品", type = "INSERT")
    public Result<Boolean> save(@RequestBody Product product) {
        return Result.success(productMapper.insert(product) > 0);
    }
}

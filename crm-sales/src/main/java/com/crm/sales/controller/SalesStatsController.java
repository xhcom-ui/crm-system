package com.crm.sales.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.crm.common.result.Result;
import com.crm.sales.entity.Product;
import com.crm.sales.entity.SalesOrder;
import com.crm.sales.mapper.ProductMapper;
import com.crm.sales.mapper.SalesOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sales/stats")
@RequiredArgsConstructor
public class SalesStatsController {
    private final SalesOrderMapper orderMapper;
    private final ProductMapper productMapper;

    @GetMapping("/report")
    @SentinelResource(value = "salesStatsReport", blockHandler = "reportBlock")
    public Result<Map<String, Object>> report() {
        List<SalesOrder> orders = orderMapper.selectList(new LambdaQueryWrapper<>());
        long productCount = productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1));
        BigDecimal salesAmount = orders.stream()
                .map(SalesOrder::getAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("metrics", List.of(
                metric("销售额", "¥" + salesAmount, "订单累计金额"),
                metric("成交订单", orders.size(), "合同已签约"),
                metric("上架产品", productCount, "可售产品数"),
                metric("回款率", "82%", "目标 85%")
        ));
        result.put("trend", List.of(
                month("1月", 82, 62), month("2月", 96, 80), month("3月", 118, 93),
                month("4月", 126, 108), month("5月", 151, 122), month("6月", 186, 152)
        ));
        result.put("composition", List.of(
                channel("软件订阅", 48), channel("增值服务", 24), channel("营销插件", 18), channel("交付服务", 10)
        ));
        return Result.success(result);
    }

    public Result<Map<String, Object>> reportBlock(BlockException ex) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("metrics", List.of(
                metric("销售额", "--", "系统繁忙，展示降级数据"),
                metric("成交订单", "--", "稍后自动恢复"),
                metric("上架产品", "--", "稍后自动恢复"),
                metric("回款率", "--", "稍后自动恢复")
        ));
        result.put("trend", List.of());
        result.put("composition", List.of());
        result.put("degraded", true);
        return Result.success("销售统计服务繁忙，已返回降级数据", result);
    }

    private Map<String, Object> metric(String label, Object value, String hint) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        item.put("hint", hint);
        return item;
    }

    private Map<String, Object> month(String month, int sales, int received) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("month", month);
        item.put("sales", sales);
        item.put("received", received);
        return item;
    }

    private Map<String, Object> channel(String name, int value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }
}

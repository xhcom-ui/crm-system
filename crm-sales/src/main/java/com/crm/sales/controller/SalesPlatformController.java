package com.crm.sales.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.log.OperationLog;
import com.crm.common.result.Result;
import com.crm.sales.entity.SalesOrder;
import com.crm.sales.entity.SalesPlatformConfig;
import com.crm.sales.entity.SalesPlatformRefund;
import com.crm.sales.entity.SalesPlatformSyncLog;
import com.crm.sales.mapper.SalesOrderMapper;
import com.crm.sales.mapper.SalesPlatformConfigMapper;
import com.crm.sales.mapper.SalesPlatformRefundMapper;
import com.crm.sales.mapper.SalesPlatformSyncLogMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sales/platform")
@RequiredArgsConstructor
public class SalesPlatformController {

    private final SalesPlatformConfigMapper configMapper;
    private final SalesPlatformSyncLogMapper syncLogMapper;
    private final SalesPlatformRefundMapper refundMapper;
    private final SalesOrderMapper orderMapper;

    @GetMapping("/supported")
    @SaCheckPermission("sales-platform:list")
    public Result<List<Map<String, String>>> supportedPlatforms() {
        return Result.success(List.of(
                platform("taobao", "淘宝"),
                platform("tmall", "天猫"),
                platform("jd", "京东"),
                platform("douyin", "抖音"),
                platform("amazon", "亚马逊"),
                platform("pdd", "拼多多")
        ));
    }

    @GetMapping("/configs")
    @SaCheckPermission("sales-platform:list")
    public Result<IPage<SalesPlatformConfig>> configs(@RequestParam(defaultValue = "1") Integer current,
                                                      @RequestParam(defaultValue = "10") Integer size,
                                                      @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SalesPlatformConfig> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(SalesPlatformConfig::getPlatformName, keyword)
                    .or().like(SalesPlatformConfig::getShopName, keyword));
        }
        wrapper.orderByDesc(SalesPlatformConfig::getUpdateTime);
        return Result.success(configMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @PostMapping("/config")
    @SaCheckPermission("sales-platform:config")
    @OperationLog(title = "新增销售平台配置", type = "INSERT")
    public Result<Boolean> addConfig(@RequestBody SalesPlatformConfig config) {
        if (config.getStatus() == null) config.setStatus(1);
        return Result.success(configMapper.insert(config) > 0);
    }

    @PutMapping("/config/{id}")
    @SaCheckPermission("sales-platform:config")
    @OperationLog(title = "更新销售平台配置", type = "UPDATE")
    public Result<Boolean> updateConfig(@PathVariable Long id, @RequestBody SalesPlatformConfig config) {
        config.setId(id);
        return Result.success(configMapper.updateById(config) > 0);
    }

    @PostMapping("/config/{id}/sync")
    @SaCheckPermission("sales-platform:sync")
    @OperationLog(title = "抽取平台订单退款数据", type = "SYNC")
    public Result<Map<String, Object>> sync(@PathVariable Long id, @RequestBody(required = false) SyncRequest request) {
        SalesPlatformConfig config = configMapper.selectById(id);
        if (config == null) return Result.success(Map.of("orderCount", 0, "refundCount", 0, "message", "平台配置不存在"));

        LocalDateTime startedAt = LocalDateTime.now();
        String syncType = request == null || request.getSyncType() == null ? "ALL" : request.getSyncType();
        int orderCount = syncType.equals("REFUND") ? 0 : upsertMockOrder(config);
        int refundCount = syncType.equals("ORDER") ? 0 : upsertMockRefund(config);

        SalesPlatformSyncLog log = new SalesPlatformSyncLog();
        log.setConfigId(id);
        log.setPlatformCode(config.getPlatformCode());
        log.setPlatformName(config.getPlatformName());
        log.setSyncType(syncType);
        log.setOrderCount(orderCount);
        log.setRefundCount(refundCount);
        log.setStatus(1);
        log.setMessage("已抽取 " + orderCount + " 条订单，" + refundCount + " 条退款");
        log.setStartedAt(startedAt);
        log.setFinishedAt(LocalDateTime.now());
        syncLogMapper.insert(log);

        config.setLastSyncTime(log.getFinishedAt());
        configMapper.updateById(config);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderCount", orderCount);
        result.put("refundCount", refundCount);
        result.put("message", log.getMessage());
        return Result.success(result);
    }

    @GetMapping("/refunds")
    @SaCheckPermission("sales-platform:list")
    public Result<IPage<SalesPlatformRefund>> refunds(@RequestParam(defaultValue = "1") Integer current,
                                                      @RequestParam(defaultValue = "10") Integer size,
                                                      @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SalesPlatformRefund> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(SalesPlatformRefund::getRefundNo, keyword)
                    .or().like(SalesPlatformRefund::getOrderNo, keyword)
                    .or().like(SalesPlatformRefund::getPlatformOrderNo, keyword));
        }
        wrapper.orderByDesc(SalesPlatformRefund::getRefundTime);
        return Result.success(refundMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @GetMapping("/sync-logs")
    @SaCheckPermission("sales-platform:list")
    public Result<IPage<SalesPlatformSyncLog>> syncLogs(@RequestParam(defaultValue = "1") Integer current,
                                                       @RequestParam(defaultValue = "10") Integer size) {
        LambdaQueryWrapper<SalesPlatformSyncLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SalesPlatformSyncLog::getStartedAt);
        return Result.success(syncLogMapper.selectPage(new Page<>(current, size), wrapper));
    }

    private int upsertMockOrder(SalesPlatformConfig config) {
        String orderNo = "EXT-" + config.getPlatformCode() + "-" + LocalDate.now().toString().replace("-", "");
        SalesOrder order = orderMapper.selectOne(new LambdaQueryWrapper<SalesOrder>().eq(SalesOrder::getOrderNo, orderNo));
        if (order == null) order = new SalesOrder();
        order.setOrderNo(orderNo);
        order.setCustomerName(config.getShopName() + " 客户");
        order.setProductName(config.getPlatformName() + " 平台商品");
        order.setAmount(new BigDecimal("199.00"));
        order.setPlatform(config.getPlatformName());
        order.setPlatformOrderNo("P-" + config.getPlatformCode() + "-" + System.currentTimeMillis());
        order.setOrderType("ORDER");
        order.setRefundAmount(BigDecimal.ZERO);
        order.setRefundStatus(0);
        order.setPaidTime(LocalDateTime.now().minusHours(2));
        order.setExtractedAt(LocalDateTime.now());
        order.setSignDate(LocalDate.now());
        order.setStatus(2);
        order.setOwnerName("平台抽取");
        order.setRemark("由平台配置抽取生成");
        if (order.getId() == null) orderMapper.insert(order);
        else orderMapper.updateById(order);
        return 1;
    }

    private int upsertMockRefund(SalesPlatformConfig config) {
        String refundNo = "RF-" + config.getPlatformCode() + "-" + LocalDate.now().toString().replace("-", "");
        SalesPlatformRefund refund = refundMapper.selectOne(new LambdaQueryWrapper<SalesPlatformRefund>().eq(SalesPlatformRefund::getRefundNo, refundNo));
        if (refund == null) refund = new SalesPlatformRefund();
        refund.setRefundNo(refundNo);
        refund.setOrderNo("EXT-" + config.getPlatformCode() + "-" + LocalDate.now().toString().replace("-", ""));
        refund.setPlatformOrderNo("P-" + config.getPlatformCode() + "-REFUND");
        refund.setPlatformCode(config.getPlatformCode());
        refund.setPlatformName(config.getPlatformName());
        refund.setShopName(config.getShopName());
        refund.setAmount(new BigDecimal("19.90"));
        refund.setReason("平台售后退款");
        refund.setStatus(2);
        refund.setRefundTime(LocalDateTime.now().minusMinutes(30));
        refund.setRawPayload("{\"source\":\"mock-extractor\"}");
        if (refund.getId() == null) refundMapper.insert(refund);
        else refundMapper.updateById(refund);
        return 1;
    }

    private Map<String, String> platform(String code, String name) {
        Map<String, String> item = new LinkedHashMap<>();
        item.put("code", code);
        item.put("name", name);
        return item;
    }

    @Data
    public static class SyncRequest {
        private String syncType;
    }
}

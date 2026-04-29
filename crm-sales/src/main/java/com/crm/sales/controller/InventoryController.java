package com.crm.sales.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.log.OperationLog;
import com.crm.common.result.Result;
import com.crm.sales.entity.InventoryMovement;
import com.crm.sales.entity.InventorySnapshot;
import com.crm.sales.entity.InventoryStock;
import com.crm.sales.mapper.InventoryMovementMapper;
import com.crm.sales.mapper.InventorySnapshotMapper;
import com.crm.sales.mapper.InventoryStockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sales/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryStockMapper stockMapper;
    private final InventorySnapshotMapper snapshotMapper;
    private final InventoryMovementMapper movementMapper;

    @GetMapping("/overview")
    @SaCheckPermission("inventory:list")
    public Result<Map<String, Object>> overview() {
        List<InventoryStock> stocks = stockMapper.selectList(new LambdaQueryWrapper<>());
        int closingQty = stocks.stream().mapToInt(item -> safe(item.getClosingQty())).sum();
        int availableQty = stocks.stream().mapToInt(item -> safe(item.getAvailableQty())).sum();
        int lowStock = (int) stocks.stream().filter(item -> safe(item.getAvailableQty()) <= safe(item.getSafetyStock())).count();
        BigDecimal stockAmount = stocks.stream().map(InventoryStock::getStockAmount).filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("metrics", List.of(
                metric("期末库存", closingQty, "当前账面数量"),
                metric("可用库存", availableQty, "扣除锁定库存"),
                metric("库存金额", "¥" + stockAmount, "按成本计价"),
                metric("低库存", lowStock, "低于安全库存")
        ));
        result.put("missingCapabilities", List.of("采购审批", "批次效期管理", "盘点差异处理", "多仓调拨", "供应商协同"));
        result.put("capabilities", List.of(
                capability("采购审批", "采购申请、审批流、入库联动和供应商报价比价。", "/sales/inventory/purchase-approval"),
                capability("批次效期管理", "按批次号、生产日期、有效期管理库存先进先出。", "/sales/inventory/batches"),
                capability("盘点差异处理", "盘点任务、实盘数量、差异审批和自动生成调整流水。", "/sales/inventory/stocktaking"),
                capability("多仓调拨", "仓库间调拨申请、出库、在途、入库闭环。", "/sales/inventory/transfer"),
                capability("供应商协同", "供应商档案、交期、对账和补货协同。", "/sales/inventory/supplier-collaboration")
        ));
        return Result.success(result);
    }

    @GetMapping("/stocks")
    @SaCheckPermission("inventory:list")
    public Result<IPage<InventoryStock>> stocks(@RequestParam(defaultValue = "1") Integer current,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) String keyword,
                                                @RequestParam(required = false) String warehouseCode) {
        LambdaQueryWrapper<InventoryStock> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(InventoryStock::getProductName, keyword)
                    .or().like(InventoryStock::getProductCode, keyword));
        }
        if (warehouseCode != null && !warehouseCode.isBlank()) wrapper.eq(InventoryStock::getWarehouseCode, warehouseCode);
        wrapper.orderByAsc(InventoryStock::getWarehouseCode).orderByAsc(InventoryStock::getProductCode);
        return Result.success(stockMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @GetMapping("/previous")
    @SaCheckPermission("inventory:list")
    public Result<List<InventorySnapshot>> previous(@RequestParam(required = false) String period) {
        LambdaQueryWrapper<InventorySnapshot> wrapper = new LambdaQueryWrapper<>();
        if (period != null && !period.isBlank()) wrapper.eq(InventorySnapshot::getPeriod, period);
        wrapper.eq(InventorySnapshot::getSnapshotType, 1)
                .orderByDesc(InventorySnapshot::getPeriod)
                .orderByAsc(InventorySnapshot::getProductCode);
        return Result.success(snapshotMapper.selectList(wrapper));
    }

    @GetMapping("/movements")
    @SaCheckPermission("inventory:list")
    public Result<IPage<InventoryMovement>> movements(@RequestParam(defaultValue = "1") Integer current,
                                                      @RequestParam(defaultValue = "10") Integer size,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String movementType) {
        LambdaQueryWrapper<InventoryMovement> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(InventoryMovement::getMovementNo, keyword)
                    .or().like(InventoryMovement::getProductName, keyword)
                    .or().like(InventoryMovement::getBizNo, keyword));
        }
        if (movementType != null && !movementType.isBlank()) wrapper.eq(InventoryMovement::getMovementType, movementType);
        wrapper.orderByDesc(InventoryMovement::getMovementTime);
        return Result.success(movementMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @GetMapping("/purchasing-sales-stock")
    @SaCheckPermission("inventory:list")
    public Result<List<Map<String, Object>>> purchasingSalesStock(@RequestParam(required = false) String period) {
        LambdaQueryWrapper<InventorySnapshot> wrapper = new LambdaQueryWrapper<>();
        if (period != null && !period.isBlank()) wrapper.eq(InventorySnapshot::getPeriod, period);
        wrapper.orderByDesc(InventorySnapshot::getPeriod).orderByAsc(InventorySnapshot::getProductCode);
        return Result.success(snapshotMapper.selectList(wrapper).stream()
                .map(item -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("period", item.getPeriod());
                    row.put("productCode", item.getProductCode());
                    row.put("productName", item.getProductName());
                    row.put("openingQty", safe(item.getOpeningQty()));
                    row.put("purchaseQty", safe(item.getInboundQty()));
                    row.put("salesQty", safe(item.getOutboundQty()));
                    row.put("closingQty", safe(item.getClosingQty()));
                    row.put("stockAmount", item.getStockAmount());
                    return row;
                }).toList());
    }

    @PostMapping("/movement")
    @SaCheckPermission("inventory:movement")
    @OperationLog(title = "新增库存流水", type = "INSERT")
    public Result<Boolean> saveMovement(@RequestBody InventoryMovement movement) {
        if (movement.getMovementNo() == null || movement.getMovementNo().isBlank()) {
            movement.setMovementNo("ST" + System.currentTimeMillis());
        }
        if (movement.getAmount() == null && movement.getUnitCost() != null && movement.getQuantity() != null) {
            movement.setAmount(movement.getUnitCost().multiply(BigDecimal.valueOf(movement.getQuantity())));
        }
        return Result.success(movementMapper.insert(movement) > 0);
    }

    private int safe(Integer value) {
        return value == null ? 0 : value;
    }

    private Map<String, Object> metric(String label, Object value, String hint) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        item.put("hint", hint);
        return item;
    }

    private Map<String, Object> capability(String name, String desc, String api) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("desc", desc);
        item.put("api", api);
        item.put("status", "待建设");
        return item;
    }
}

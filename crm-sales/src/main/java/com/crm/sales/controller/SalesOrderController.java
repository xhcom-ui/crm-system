package com.crm.sales.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.log.OperationLog;
import com.crm.common.result.Result;
import com.crm.sales.entity.SalesPlatformRefund;
import com.crm.sales.entity.SalesOrder;
import com.crm.sales.mapper.SalesPlatformRefundMapper;
import com.crm.sales.mapper.SalesOrderMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/sales/order")
@RequiredArgsConstructor
public class SalesOrderController {
    private final SalesOrderMapper orderMapper;
    private final SalesPlatformRefundMapper refundMapper;

    @GetMapping("/page")
    @SaCheckPermission("sales-management:list")
    public Result<IPage<SalesOrder>> page(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SalesOrder> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(SalesOrder::getOrderNo, keyword)
                    .or().like(SalesOrder::getCustomerName, keyword)
                    .or().like(SalesOrder::getProductName, keyword)
                    .or().like(SalesOrder::getPlatformOrderNo, keyword));
        }
        wrapper.orderByDesc(SalesOrder::getSignDate).orderByDesc(SalesOrder::getCreateTime);
        return Result.success(orderMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @GetMapping("/{id}")
    @SaCheckPermission("sales-management:list")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        SalesOrder order = orderMapper.selectById(id);
        if (order == null) return Result.success(Map.of());

        LambdaQueryWrapper<SalesPlatformRefund> refundWrapper = new LambdaQueryWrapper<>();
        refundWrapper.eq(SalesPlatformRefund::getOrderNo, order.getOrderNo());
        if (order.getPlatformOrderNo() != null && !order.getPlatformOrderNo().isBlank()) {
            refundWrapper.or().eq(SalesPlatformRefund::getPlatformOrderNo, order.getPlatformOrderNo());
        }
        refundWrapper.orderByDesc(SalesPlatformRefund::getRefundTime);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("order", order);
        result.put("refunds", refundMapper.selectList(refundWrapper));
        return Result.success(result);
    }

    @PostMapping
    @SaCheckPermission("sales-management:add")
    @OperationLog(title = "新增销售订单", type = "INSERT")
    public Result<Boolean> save(@RequestBody SalesOrder order) {
        return Result.success(orderMapper.insert(order) > 0);
    }

    @PostMapping("/import")
    @SaCheckPermission("sales-management:import")
    @OperationLog(title = "导入销售订单", type = "IMPORT")
    public Result<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file) throws Exception {
        List<String> errors = new ArrayList<>();
        int total = 0;
        int success = 0;

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() < 2) {
                return Result.success(importResult(0, 0, List.of("文件没有可导入的数据")));
            }
            Map<String, Integer> header = readHeader(sheet.getRow(0));
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isBlankRow(row)) continue;
                total++;
                try {
                    SalesOrder order = parseOrder(row, header);
                    if (order.getOrderNo() == null || order.getOrderNo().isBlank()) {
                        order.setOrderNo("SO" + System.currentTimeMillis() + i);
                    }
                    if (order.getCustomerName() == null || order.getCustomerName().isBlank()) {
                        errors.add("第 " + (i + 1) + " 行缺少客户名称");
                        continue;
                    }
                    if (order.getProductName() == null || order.getProductName().isBlank()) {
                        errors.add("第 " + (i + 1) + " 行缺少产品/服务");
                        continue;
                    }
                    if (order.getStatus() == null) order.setStatus(1);
                    if (order.getOrderType() == null || order.getOrderType().isBlank()) order.setOrderType("ORDER");

                    LambdaQueryWrapper<SalesOrder> exists = new LambdaQueryWrapper<SalesOrder>()
                            .eq(SalesOrder::getOrderNo, order.getOrderNo());
                    SalesOrder old = orderMapper.selectOne(exists);
                    if (old == null) {
                        orderMapper.insert(order);
                    } else {
                        order.setId(old.getId());
                        orderMapper.updateById(order);
                    }
                    success++;
                } catch (Exception e) {
                    errors.add("第 " + (i + 1) + " 行导入失败：" + e.getMessage());
                }
            }
        }

        return Result.success(importResult(total, success, errors));
    }

    private Map<String, Object> importResult(int total, int success, List<String> errors) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("success", success);
        result.put("failed", Math.max(total - success, errors.size()));
        result.put("errors", errors);
        return result;
    }

    private Map<String, Integer> readHeader(Row row) {
        Map<String, Integer> header = new HashMap<>();
        if (row == null) return header;
        for (Cell cell : row) {
            String name = cellValue(cell).trim();
            if (!name.isBlank()) header.put(normalize(name), cell.getColumnIndex());
        }
        return header;
    }

    private SalesOrder parseOrder(Row row, Map<String, Integer> header) {
        SalesOrder order = new SalesOrder();
        order.setOrderNo(read(row, header, "订单编号", "订单号", "orderNo"));
        order.setCustomerName(read(row, header, "客户", "客户名称", "customerName"));
        order.setProductName(read(row, header, "产品", "产品/服务", "商品名称", "productName"));
        order.setAmount(decimal(read(row, header, "金额", "合同金额", "实付金额", "amount")));
        order.setOwnerName(read(row, header, "销售", "负责人", "ownerName"));
        order.setSignDate(date(read(row, header, "签约日期", "下单日期", "订单日期", "signDate")));
        order.setStatus(status(read(row, header, "状态", "订单状态", "status")));
        order.setPlatform(read(row, header, "平台", "platform"));
        order.setPlatformOrderNo(read(row, header, "平台订单号", "外部订单号", "platformOrderNo"));
        order.setOrderType(read(row, header, "订单类型", "orderType"));
        order.setRefundAmount(decimal(read(row, header, "退款金额", "refundAmount")));
        order.setRefundStatus(refundStatus(read(row, header, "退款状态", "refundStatus")));
        order.setPaidTime(dateTime(read(row, header, "支付时间", "paidTime")));
        order.setRemark(read(row, header, "备注", "remark"));
        return order;
    }

    private String read(Row row, Map<String, Integer> header, String... names) {
        for (String name : names) {
            Integer index = header.get(normalize(name));
            if (index != null) return cellValue(row.getCell(index)).trim();
        }
        return "";
    }

    private boolean isBlankRow(Row row) {
        for (Cell cell : row) {
            if (!cellValue(cell).trim().isBlank()) return false;
        }
        return true;
    }

    private String cellValue(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter(Locale.CHINA);
        return formatter.formatCellValue(cell);
    }

    private String normalize(String value) {
        return value == null ? "" : value.replaceAll("[\\s_\\-/]", "").toLowerCase(Locale.ROOT);
    }

    private BigDecimal decimal(String value) {
        if (value == null || value.isBlank()) return BigDecimal.ZERO;
        String normalized = value.replace(",", "").replace("¥", "").trim();
        return new BigDecimal(normalized);
    }

    private LocalDate date(String value) {
        LocalDateTime dateTime = dateTime(value);
        return dateTime == null ? null : dateTime.toLocalDate();
    }

    private LocalDateTime dateTime(String value) {
        if (value == null || value.isBlank()) return null;
        String text = value.trim().replace("/", "-");
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDateTime.parse(text, formatter);
            } catch (Exception ignored) {
            }
        }
        try {
            return LocalDate.parse(text, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
        } catch (Exception ignored) {
        }
        try {
            Date date = DateUtil.getJavaDate(Double.parseDouble(text));
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer status(String value) {
        if (value == null || value.isBlank()) return 1;
        if (value.matches("\\d+")) return Integer.parseInt(value);
        if (value.contains("完成") || value.contains("成功")) return 3;
        if (value.contains("回款") || value.contains("支付")) return 2;
        return 1;
    }

    private Integer refundStatus(String value) {
        if (value == null || value.isBlank()) return 0;
        if (value.matches("\\d+")) return Integer.parseInt(value);
        if (value.contains("成功") || value.contains("完成")) return 2;
        if (value.contains("中") || value.contains("处理")) return 1;
        return 0;
    }
}

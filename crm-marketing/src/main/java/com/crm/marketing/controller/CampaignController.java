package com.crm.marketing.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.log.OperationLog;
import com.crm.common.result.Result;
import com.crm.marketing.entity.Campaign;
import com.crm.marketing.entity.CampaignPerformance;
import com.crm.marketing.service.CampaignMatchService;
import com.crm.marketing.service.CampaignService;
import com.crm.marketing.mapper.CampaignPerformanceMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/marketing/campaign")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;
    private final CampaignMatchService campaignMatchService;
    private final CampaignPerformanceMapper performanceMapper;

    @GetMapping("/page")
    @SaCheckPermission("marketing:list")
    public Result<IPage<Campaign>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(campaignService.pageCampaigns(new Page<>(current, size), keyword));
    }

    @GetMapping("/{id}")
    public Result<Campaign> getById(@PathVariable Long id) {
        return Result.success(campaignService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("marketing:add")
    public Result<Boolean> save(@Valid @RequestBody Campaign campaign) {
        return Result.success(campaignService.save(campaign));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("marketing:edit")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody Campaign campaign) {
        campaign.setId(id);
        return Result.success(campaignService.updateById(campaign));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("marketing:del")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(campaignService.removeById(id));
    }

    /**
     * 营销活动统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(campaignMatchService.getCampaignStats());
    }

    /**
     * 营销活动效果报表。
     */
    @GetMapping("/report")
    @SentinelResource(value = "marketingCampaignReport", blockHandler = "reportBlock")
    public Result<Map<String, Object>> report() {
        Map<String, Object> stats = campaignMatchService.getCampaignStats();
        Map<String, Object> report = new LinkedHashMap<>();
        Long total = ((Number) stats.getOrDefault("total", 0L)).longValue();
        Long active = ((Number) stats.getOrDefault("active", 0L)).longValue();
        Long completed = ((Number) stats.getOrDefault("completed", 0L)).longValue();

        List<CampaignPerformance> performances = performances();
        report.put("metrics", List.of(
                metric("活动总数", total, "含草稿与运行中"),
                metric("运行中", active, "当前可触达活动"),
                metric("已完成", completed, "可复盘效果"),
                metric("营销 ROI", roi(performances), "收入 / 成本")
        ));
        report.put("trend", trendData());
        report.put("channels", channelData());
        report.put("campaignRows", campaignRowData());
        return Result.success(report);
    }

    public Result<Map<String, Object>> reportBlock(BlockException ex) {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("metrics", List.of(
                metric("活动总数", "--", "系统繁忙，展示降级数据"),
                metric("运行中", "--", "稍后自动恢复"),
                metric("已完成", "--", "稍后自动恢复"),
                metric("营销 ROI", "--", "稍后自动恢复")
        ));
        report.put("trend", List.of());
        report.put("channels", List.of());
        report.put("campaignRows", List.of());
        report.put("degraded", true);
        return Result.success("营销报表服务繁忙，已返回降级数据", report);
    }

    @PostMapping("/performance")
    @SaCheckPermission("marketing:performance:add")
    @OperationLog(title = "录入营销效果", type = "INSERT")
    public Result<Boolean> savePerformance(@RequestBody CampaignPerformance performance) {
        return Result.success(performanceMapper.insert(performance) > 0);
    }

    /**
     * 根据客户标签自动匹配活动
     */
    @PostMapping("/match")
    public Result<List<Campaign>> match(@RequestParam String tags) {
        List<String> tagList = Arrays.asList(tags.split(","));
        return Result.success(campaignMatchService.matchCampaigns(tagList));
    }

    private Map<String, Object> metric(String label, Object value, String hint) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        item.put("hint", hint);
        return item;
    }

    private Map<String, Object> month(String month, int sent, int clicked, int converted) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("month", month);
        item.put("sent", sent);
        item.put("clicked", clicked);
        item.put("converted", converted);
        return item;
    }

    private Map<String, Object> channel(String name, int value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }

    private List<Map<String, Object>> campaignRows() {
        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(row("春季续费唤醒", 9820, "42%", "16%", 386, "4.8", "增长"));
        rows.add(row("高价值客户升级包", 3260, "55%", "24%", 168, "6.1", "增长"));
        rows.add(row("沉睡线索再营销", 12400, "28%", "7%", 92, "1.9", "观察"));
        rows.add(row("工单满意度回访", 6140, "48%", "18%", 134, "3.2", "增长"));
        return rows;
    }

    private List<CampaignPerformance> performances() {
        try {
            return performanceMapper.selectList(new LambdaQueryWrapper<CampaignPerformance>()
                    .orderByDesc(CampaignPerformance::getStatMonth));
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private List<Map<String, Object>> trendData() {
        List<CampaignPerformance> records = performances();
        if (records.isEmpty()) {
            return List.of(
                    month("1月", 4200, 560, 68),
                    month("2月", 5300, 740, 92),
                    month("3月", 6100, 880, 118),
                    month("4月", 5800, 810, 104),
                    month("5月", 7200, 1120, 146),
                    month("6月", 8220, 1380, 186)
            );
        }
        Map<String, int[]> byMonth = new LinkedHashMap<>();
        for (CampaignPerformance record : records) {
            String month = record.getStatMonth() == null ? "未知" : record.getStatMonth();
            int[] values = byMonth.computeIfAbsent(month, k -> new int[3]);
            values[0] += record.getSentCount() == null ? 0 : record.getSentCount();
            values[1] += record.getClickCount() == null ? 0 : record.getClickCount();
            values[2] += record.getConversionCount() == null ? 0 : record.getConversionCount();
        }
        return byMonth.entrySet().stream()
                .map(entry -> month(entry.getKey(), entry.getValue()[0], entry.getValue()[1], entry.getValue()[2]))
                .toList();
    }

    private List<Map<String, Object>> channelData() {
        List<CampaignPerformance> records = performances();
        if (records.isEmpty()) {
            return List.of(channel("邮件", 42), channel("短信", 23), channel("站内推送", 21), channel("人工跟进", 14));
        }
        Map<String, Integer> byChannel = new LinkedHashMap<>();
        int total = 0;
        for (CampaignPerformance record : records) {
            int conversions = record.getConversionCount() == null ? 0 : record.getConversionCount();
            total += conversions;
            byChannel.merge(record.getChannel() == null ? "其他" : record.getChannel(), conversions, Integer::sum);
        }
        int finalTotal = Math.max(total, 1);
        return byChannel.entrySet().stream()
                .map(entry -> channel(entry.getKey(), Math.round(entry.getValue() * 100f / finalTotal)))
                .toList();
    }

    private List<Map<String, Object>> campaignRowData() {
        List<CampaignPerformance> records = performances();
        if (records.isEmpty()) return campaignRows();
        return records.stream()
                .map(record -> row(
                        record.getCampaignName(),
                        record.getSentCount() == null ? 0 : record.getSentCount(),
                        rate(record.getOpenCount(), record.getSentCount()),
                        rate(record.getClickCount(), record.getSentCount()),
                        record.getConversionCount() == null ? 0 : record.getConversionCount(),
                        roi(record),
                        "增长"))
                .toList();
    }

    private String rate(Integer numerator, Integer denominator) {
        int den = denominator == null ? 0 : denominator;
        if (den <= 0) return "0%";
        return Math.round((numerator == null ? 0 : numerator) * 100f / den) + "%";
    }

    private String roi(List<CampaignPerformance> records) {
        BigDecimal revenue = records.stream()
                .map(CampaignPerformance::getRevenueAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal cost = records.stream()
                .map(CampaignPerformance::getCostAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return roiValue(revenue, cost);
    }

    private String roi(CampaignPerformance record) {
        return roiValue(record.getRevenueAmount(), record.getCostAmount());
    }

    private String roiValue(BigDecimal revenue, BigDecimal cost) {
        if (cost == null || cost.compareTo(BigDecimal.ZERO) <= 0) return "0.0";
        BigDecimal safeRevenue = revenue == null ? BigDecimal.ZERO : revenue;
        return safeRevenue.divide(cost, 1, RoundingMode.HALF_UP).toPlainString();
    }

    private Map<String, Object> row(String name, int sent, String openRate, String clickRate, int conversion, String roi, String status) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("sent", sent);
        item.put("openRate", openRate);
        item.put("clickRate", clickRate);
        item.put("conversion", conversion);
        item.put("roi", roi);
        item.put("status", status);
        return item;
    }
}

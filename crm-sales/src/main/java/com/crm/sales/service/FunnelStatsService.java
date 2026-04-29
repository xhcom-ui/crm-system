package com.crm.sales.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.sales.entity.Opportunity;
import com.crm.sales.mapper.OpportunityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * 销售漏斗统计服务
 */
@Service
@RequiredArgsConstructor
public class FunnelStatsService {

    private final OpportunityMapper opportunityMapper;

    // 销售阶段映射
    private static final Map<Integer, String> STAGE_NAMES = Map.of(
            1, "初步接触", 2, "需求分析", 3, "方案报价",
            4, "谈判", 5, "赢单", 6, "输单"
    );

    /**
     * 获取销售漏斗数据（各阶段商机数量 + 金额汇总）
     */
    public Map<String, Object> getFunnelData() {
        List<Opportunity> all = opportunityMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> stages = new ArrayList<>();
        long totalCount = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Map.Entry<Integer, String> entry : STAGE_NAMES.entrySet()) {
            int stage = entry.getKey();
            String name = entry.getValue();

            List<Opportunity> stageList = all.stream()
                    .filter(o -> o.getStage() != null && o.getStage() == stage)
                    .toList();

            long count = stageList.size();
            BigDecimal amount = stageList.stream()
                    .map(o -> o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalCount += count;
            totalAmount = totalAmount.add(amount);

            Map<String, Object> stageData = new LinkedHashMap<>();
            stageData.put("stage", stage);
            stageData.put("name", name);
            stageData.put("count", count);
            stageData.put("amount", amount);
            stages.add(stageData);
        }

        result.put("stages", stages);
        result.put("totalCount", totalCount);
        result.put("totalAmount", totalAmount);
        return result;
    }

    /**
     * 获取赢单率统计
     */
    public Map<String, Object> getWinRate() {
        Long total = opportunityMapper.selectCount(new LambdaQueryWrapper<>());
        Long won = opportunityMapper.selectCount(
                new LambdaQueryWrapper<Opportunity>().eq(Opportunity::getStage, 5));
        Long lost = opportunityMapper.selectCount(
                new LambdaQueryWrapper<Opportunity>().eq(Opportunity::getStage, 6));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("won", won);
        result.put("lost", lost);
        result.put("winRate", total > 0 ? (double) won / (won + (lost > 0 ? lost : 0)) * 100 : 0);
        return result;
    }
}

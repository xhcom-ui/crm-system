package com.crm.leads.service;

import com.crm.leads.entity.Lead;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 线索评分引擎
 * 根据来源权重 + 关键词匹配 + 行为评分 计算线索质量分
 */
@Component
public class LeadScoringEngine {

    // 来源权重映射
    private static final Map<Integer, Integer> SOURCE_WEIGHTS = Map.of(
            1, 30,  // 官网 - 高质量
            2, 20,  // 广告
            3, 25,  // 推荐 - 较高质量
            4, 15,  // 社交媒体
            5, 10   // 其他
    );

    // 关键词匹配加分（出现即加分）
    private static final Map<String, Integer> KEYWORD_SCORES = Map.of(
            "企业", 15, "采购", 20, "合作", 10,
            "急", 25, "需求", 10, "项目", 5,
            "预算", 20, "决策", 15, "demo", 10
    );

    /**
     * 计算线索评分（0-100）
     */
    public int calculateScore(Lead lead) {
        int score = 0;

        // 1. 来源权重（0-30分）
        if (lead.getSource() != null) {
            score += SOURCE_WEIGHTS.getOrDefault(lead.getSource(), 10);
        }

        // 2. 关键词匹配（0-50分）
        if (lead.getName() != null) {
            for (Map.Entry<String, Integer> entry : KEYWORD_SCORES.entrySet()) {
                if (lead.getName().toLowerCase().contains(entry.getKey())) {
                    score += entry.getValue();
                }
            }
        }

        // 3. 基础分（确保新线索也有分数）
        score += 10;

        // 4. 上限100
        return Math.min(score, 100);
    }

    /**
     * 对线索进行评分并更新
     */
    public Lead scoreLead(Lead lead) {
        int score = calculateScore(lead);
        lead.setScore(score);

        // 高评分线索自动标记
        if (score >= 70) {
            // 高评分线索，可触发实时通知
        }
        return lead;
    }
}

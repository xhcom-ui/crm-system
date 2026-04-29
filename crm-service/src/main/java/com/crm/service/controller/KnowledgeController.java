package com.crm.service.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.crm.common.result.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.log.OperationLog;
import com.crm.service.entity.KnowledgeArticle;
import com.crm.service.entity.SlaRule;
import com.crm.service.mapper.KnowledgeArticleMapper;
import com.crm.service.mapper.SlaRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SLA 与知识库接口。
 */
@RestController
@RequestMapping("/service/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeArticleMapper articleMapper;
    private final SlaRuleMapper slaRuleMapper;

    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("slaCards", slaCards());
        result.put("articles", articleData());
        return Result.success(result);
    }

    @GetMapping("/articles")
    public Result<List<Map<String, Object>>> articlesList() {
        return Result.success(articleData());
    }

    @PostMapping("/articles")
    @SaCheckPermission("service:knowledge:add")
    @OperationLog(title = "新增知识库文章", type = "INSERT")
    public Result<Boolean> saveArticle(@RequestBody KnowledgeArticle article) {
        return Result.success(articleMapper.insert(article) > 0);
    }

    @GetMapping("/sla-rules")
    public Result<List<Map<String, Object>>> slaRules() {
        return Result.success(slaCards());
    }

    @PostMapping("/sla-rules")
    @SaCheckPermission("service:sla:add")
    @OperationLog(title = "新增SLA规则", type = "INSERT")
    public Result<Boolean> saveSlaRule(@RequestBody SlaRule rule) {
        return Result.success(slaRuleMapper.insert(rule) > 0);
    }

    private List<Map<String, Object>> slaCards() {
        try {
            List<SlaRule> rules = slaRuleMapper.selectList(new LambdaQueryWrapper<SlaRule>()
                    .eq(SlaRule::getStatus, 1)
                    .orderByAsc(SlaRule::getPriority));
            if (!rules.isEmpty()) {
                return rules.stream().map(this::slaMap).toList();
            }
        } catch (Exception ignored) {
            // 兼容未执行新增表迁移的环境。
        }
        return List.of(
                sla("紧急工单", "15 分钟", "超时自动升级主管", "critical"),
                sla("高优先级", "1 小时", "需当天给出方案", "high"),
                sla("普通咨询", "4 小时", "工作时间内响应", "normal"),
                sla("建议反馈", "1 天", "进入需求池评估", "low")
        );
    }

    private Map<String, Object> slaMap(SlaRule rule) {
        return sla(
                rule.getTitle(),
                formatMinutes(rule.getResponseMinutes()),
                rule.getDescription(),
                rule.getTone() == null ? "normal" : rule.getTone());
    }

    private String formatMinutes(Integer minutes) {
        if (minutes == null) return "-";
        if (minutes < 60) return minutes + " 分钟";
        if (minutes % 60 == 0) return (minutes / 60) + " 小时";
        return minutes + " 分钟";
    }

    private List<Map<String, Object>> articles() {
        return List.of(
                article("数据导入失败排查", "数据服务", List.of("导入", "模板", "字段映射"), 128,
                        "先确认模板版本和必填字段，再检查手机号、邮箱等唯一字段是否重复。",
                        List.of("下载最新模板", "校验必填字段", "检查重复数据", "重新发起导入任务")),
                article("客户无法收到营销邮件", "营销触达", List.of("邮件", "退订", "送达率"), 96,
                        "检查客户是否退订、邮箱是否无效，以及活动发送域名是否通过校验。",
                        List.of("确认订阅状态", "验证邮箱格式", "检查发送记录", "切换备用触达渠道")),
                article("工作流节点未自动流转", "自动化", List.of("工作流", "节点", "审批"), 72,
                        "确认工作流启用状态、节点顺序和触发条件，必要时手动重试实例。",
                        List.of("检查工作流状态", "查看实例日志", "确认节点配置", "重新触发流程"))
        );
    }

    private List<Map<String, Object>> articleData() {
        try {
            List<KnowledgeArticle> records = articleMapper.selectList(
                    new LambdaQueryWrapper<KnowledgeArticle>()
                            .eq(KnowledgeArticle::getStatus, 1)
                            .orderByDesc(KnowledgeArticle::getUsedCount));
            if (!records.isEmpty()) {
                return records.stream().map(this::articleMap).toList();
            }
        } catch (Exception ignored) {
            // 兼容未执行新增表迁移的环境。
        }
        return articles();
    }

    private Map<String, Object> articleMap(KnowledgeArticle article) {
        return article(
                article.getTitle(),
                article.getCategory(),
                split(article.getTags()),
                article.getUsedCount() == null ? 0 : article.getUsedCount(),
                article.getSolution(),
                split(article.getSteps()));
    }

    private List<String> split(String value) {
        if (value == null || value.isBlank()) return List.of();
        return List.of(value.split(","));
    }

    private Map<String, Object> sla(String title, String time, String desc, String tone) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", title);
        item.put("time", time);
        item.put("desc", desc);
        item.put("tone", tone);
        return item;
    }

    private Map<String, Object> article(String title, String category, List<String> tags, int used, String solution, List<String> steps) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("title", title);
        item.put("category", category);
        item.put("tags", tags);
        item.put("used", used);
        item.put("solution", solution);
        item.put("steps", steps);
        return item;
    }
}

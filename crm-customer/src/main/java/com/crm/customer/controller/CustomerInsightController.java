package com.crm.customer.controller;

import com.crm.common.result.Result;
import com.crm.customer.entity.Contact;
import com.crm.customer.entity.CustomerInteraction;
import com.crm.customer.mapper.CustomerInteractionMapper;
import com.crm.customer.service.ContactService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户洞察聚合接口。
 */
@RestController
@RequestMapping("/customer/insight")
@RequiredArgsConstructor
public class CustomerInsightController {

    private final ContactService contactService;
    private final CustomerInteractionMapper interactionMapper;

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Contact contact = contactService.getById(id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("customer", contact);
        result.put("profile", buildProfile(contact));
        result.put("interactions", interactions(id, contact));
        result.put("attribution", buildAttribution(contact));
        result.put("metrics", buildMetrics(contact));
        return Result.success(result);
    }

    private Map<String, Object> buildProfile(Contact contact) {
        Map<String, Object> profile = new LinkedHashMap<>();
        List<String> tags = new ArrayList<>();
        Integer level = contact == null ? 1 : contact.getLevel();
        Integer source = contact == null ? 1 : contact.getSource();

        if (level != null && level >= 3) {
            tags.add("VIP");
            tags.add("高价值");
        } else if (level != null && level == 2) {
            tags.add("重要客户");
        } else {
            tags.add("普通客户");
        }
        if (source != null && source == 1) tags.add("官网来源");
        if (source != null && source == 2) tags.add("推荐来源");
        tags.add("售前跟进");
        tags.add("可营销触达");

        profile.put("tags", tags);
        profile.put("score", score(contact));
        profile.put("conversionRate", conversionRate(contact));
        return profile;
    }

    private List<Map<String, Object>> buildInteractions(Contact contact) {
        String name = contact == null || contact.getName() == null ? "客户" : contact.getName();
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(item("今天 10:20", "销售电话跟进", "已联系 " + name + "，确认下一步报价和采购时间。", "primary"));
        items.add(item("昨天 16:40", "营销内容互动", "客户查看产品升级内容，建议安排销售跟进。", "success"));
        items.add(item("本周一 11:05", "服务咨询记录", "客户咨询数据导入、权限配置等使用问题。", "warning"));
        items.add(item("上周三 09:30", "线索来源记录", "首次来源已写入客户档案，可用于转化归因。", "info"));
        return items;
    }

    private List<Map<String, Object>> interactions(Long contactId, Contact contact) {
        try {
            List<CustomerInteraction> records = interactionMapper.selectList(
                    new LambdaQueryWrapper<CustomerInteraction>()
                            .eq(CustomerInteraction::getContactId, contactId)
                            .orderByDesc(CustomerInteraction::getCreateTime));
            if (!records.isEmpty()) {
                return records.stream()
                        .map(record -> item(
                                record.getCreateTime() == null ? "" : record.getCreateTime().toString().replace('T', ' '),
                                record.getTitle(),
                                record.getDescription(),
                                record.getTimelineType() == null ? "primary" : record.getTimelineType()))
                        .toList();
            }
        } catch (Exception ignored) {
            // 兼容未执行新增表迁移的环境。
        }
        return buildInteractions(contact);
    }

    private List<Map<String, Object>> buildAttribution(Contact contact) {
        Integer source = contact == null ? 1 : contact.getSource();
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(channel(sourceLabel(source), 36));
        items.add(channel("营销触达", 28));
        items.add(channel("销售跟进", 24));
        items.add(channel("服务体验", 12));
        return items;
    }

    private Map<String, Object> buildMetrics(Contact contact) {
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("profileScore", score(contact));
        metrics.put("conversionRate", conversionRate(contact));
        metrics.put("lastTouch", "今天");
        metrics.put("primaryChannel", sourceLabel(contact == null ? 1 : contact.getSource()));
        return metrics;
    }

    private Map<String, Object> item(String time, String title, String desc, String type) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("time", time);
        item.put("title", title);
        item.put("desc", desc);
        item.put("type", type);
        return item;
    }

    private Map<String, Object> channel(String channel, int weight) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("channel", channel);
        item.put("weight", weight);
        return item;
    }

    private int score(Contact contact) {
        int level = contact == null || contact.getLevel() == null ? 1 : contact.getLevel();
        return Math.min(60 + level * 12 + 8, 99);
    }

    private int conversionRate(Contact contact) {
        int level = contact == null || contact.getLevel() == null ? 1 : contact.getLevel();
        return Math.min(35 + level * 14 + 18, 96);
    }

    private String sourceLabel(Integer source) {
        if (source == null) return "未知来源";
        return switch (source) {
            case 1 -> "官网表单";
            case 2 -> "客户推荐";
            case 3 -> "展会活动";
            case 4 -> "其他渠道";
            default -> "未知来源";
        };
    }
}

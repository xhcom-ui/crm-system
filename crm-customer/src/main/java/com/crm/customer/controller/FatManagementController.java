package com.crm.customer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.log.OperationLog;
import com.crm.common.result.Result;
import com.crm.customer.entity.Contact;
import com.crm.customer.entity.CustomerInfoItem;
import com.crm.customer.entity.CustomerInteraction;
import com.crm.customer.entity.FatManagementRecord;
import com.crm.customer.entity.FollowupRecord;
import com.crm.customer.mapper.ContactMapper;
import com.crm.customer.mapper.CustomerInfoItemMapper;
import com.crm.customer.mapper.CustomerInteractionMapper;
import com.crm.customer.mapper.FatManagementRecordMapper;
import com.crm.customer.mapper.FollowupRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer/fat-management")
@RequiredArgsConstructor
public class FatManagementController {

    private final FatManagementRecordMapper recordMapper;
    private final ContactMapper contactMapper;
    private final CustomerInteractionMapper interactionMapper;
    private final FollowupRecordMapper followupMapper;
    private final CustomerInfoItemMapper infoItemMapper;

    @GetMapping("/page")
    @SaCheckPermission("fat-management:list")
    public Result<IPage<FatManagementRecord>> page(@RequestParam(defaultValue = "1") Integer current,
                                                   @RequestParam(defaultValue = "10") Integer size,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<FatManagementRecord> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(FatManagementRecord::getCustomerName, keyword)
                    .or().like(FatManagementRecord::getPlanName, keyword)
                    .or().like(FatManagementRecord::getStage, keyword));
        }
        if (status != null) wrapper.eq(FatManagementRecord::getStatus, status);
        wrapper.orderByAsc(FatManagementRecord::getReviewDate).orderByDesc(FatManagementRecord::getCreateTime);
        return Result.success(recordMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @GetMapping("/overview")
    @SaCheckPermission("fat-management:list")
    public Result<Map<String, Object>> overview() {
        List<FatManagementRecord> records = recordMapper.selectList(new LambdaQueryWrapper<>());
        List<Contact> contacts = contactMapper.selectList(new LambdaQueryWrapper<>());
        List<CustomerInteraction> interactions = interactionMapper.selectList(new LambdaQueryWrapper<>());
        List<FollowupRecord> followups = followupMapper.selectList(new LambdaQueryWrapper<>());
        List<CustomerInfoItem> infoItems = infoItemMapper.selectList(new LambdaQueryWrapper<CustomerInfoItem>().orderByDesc(CustomerInfoItem::getCreateTime));
        List<CustomerInfoItem> identities = infoItems.stream().filter(item -> "identity".equals(item.getModuleType())).toList();
        List<CustomerInfoItem> addresses = infoItems.stream().filter(item -> "address".equals(item.getModuleType())).toList();
        List<CustomerInfoItem> relationships = infoItems.stream().filter(item -> "relationship".equals(item.getModuleType())).toList();
        List<CustomerInfoItem> websites = infoItems.stream().filter(item -> "website".equals(item.getModuleType())).toList();
        List<CustomerInfoItem> contactItems = infoItems.stream().filter(item -> "contact".equals(item.getModuleType())).toList();
        long active = records.stream().filter(item -> item.getStatus() != null && item.getStatus() == 1).count();
        long highRisk = records.stream().filter(item -> "高".equals(item.getRiskLevel())).count();
        BigDecimal avgBodyFat = average(records.stream().map(FatManagementRecord::getBodyFatRate).toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("metrics", List.of(
                metric("客户档案", contacts.size(), "统一信息主档"),
                metric("模块覆盖", moduleCount(records, contacts), "脂肪/身份/住址/关系等"),
                metric("联系记录", interactions.size() + followups.size(), "互动与跟进沉淀"),
                metric("平均体脂", avgBodyFat + "%", active + " 人执行中")
        ));
        result.put("customerRows", contacts.stream().limit(20).map(this::customerRow).toList());
        result.put("identityRows", !identities.isEmpty() ? identities.stream().limit(50).map(this::infoItemRow).toList() : contacts.stream().limit(20).map(this::identityRow).toList());
        result.put("addressRows", !addresses.isEmpty() ? addresses.stream().limit(50).map(this::infoItemRow).toList() : contacts.stream().limit(20).map(this::addressRow).toList());
        result.put("relationshipRows", !relationships.isEmpty() ? relationships.stream().limit(50).map(this::infoItemRow).toList() : contacts.stream().limit(20).map(this::relationshipRow).toList());
        result.put("websiteRows", !websites.isEmpty() ? websites.stream().limit(50).map(this::infoItemRow).toList() : contacts.stream().limit(20).map(this::websiteRow).toList());
        result.put("contactRows", !contactItems.isEmpty() ? contactItems.stream().limit(50).map(this::infoItemRow).toList() : interactions.stream().limit(20).map(this::interactionRow).toList());
        result.put("stageRows", List.of(
                stage("建档评估", countStage(records, "建档评估")),
                stage("减脂执行", countStage(records, "减脂执行")),
                stage("平台调整", countStage(records, "平台调整")),
                stage("维持巩固", countStage(records, "维持巩固"))
        ));
        result.put("moduleCards", List.of(
                module("脂肪档案", records.size(), "体脂、目标、阶段、风险"),
                module("身份信息", contacts.size(), "姓名、手机、邮箱、职位"),
                module("住址信息", contacts.size(), "省市区、详细地址、邮编"),
                module("联系记录", interactions.size() + followups.size(), "电话、邮件、会议、跟进"),
                module("关系图谱", Math.max(contacts.size() - 1, 0), "客户、企业、联系人关系"),
                module("网站博客", contacts.size(), "官网、博客、社媒和内容偏好")
        ));
        result.put("missingCapabilities", List.of("统一客户ID合并", "证件OCR识别", "地址标准化", "社交图谱自动发现", "博客舆情监测", "隐私授权管理"));
        return Result.success(result);
    }

    @PostMapping
    @SaCheckPermission("fat-management:add")
    @OperationLog(title = "新增客户信息档案", type = "INSERT")
    public Result<Boolean> save(@RequestBody FatManagementRecord record) {
        return Result.success(recordMapper.insert(record) > 0);
    }

    @PostMapping("/module/{moduleType}")
    @SaCheckPermission("fat-management:add")
    @OperationLog(title = "新增客户信息模块明细", type = "INSERT")
    public Result<Boolean> saveModuleItem(@PathVariable String moduleType, @RequestBody CustomerInfoItem item) {
        item.setModuleType(moduleType);
        return Result.success(infoItemMapper.insert(item) > 0);
    }

    @PutMapping("/module/{moduleType}/{id}")
    @SaCheckPermission("fat-management:edit")
    @OperationLog(title = "更新客户信息模块明细", type = "UPDATE")
    public Result<Boolean> updateModuleItem(@PathVariable String moduleType,
                                            @PathVariable Long id,
                                            @RequestBody CustomerInfoItem item) {
        item.setId(id);
        item.setModuleType(moduleType);
        return Result.success(infoItemMapper.updateById(item) > 0);
    }

    @DeleteMapping("/module/{moduleType}/{id}")
    @SaCheckPermission("fat-management:edit")
    @OperationLog(title = "删除客户信息模块明细", type = "DELETE")
    public Result<Boolean> deleteModuleItem(@PathVariable String moduleType, @PathVariable Long id) {
        return Result.success(infoItemMapper.delete(new LambdaQueryWrapper<CustomerInfoItem>()
                .eq(CustomerInfoItem::getId, id)
                .eq(CustomerInfoItem::getModuleType, moduleType)) > 0);
    }

    @PutMapping("/{id}")
    @SaCheckPermission("fat-management:edit")
    @OperationLog(title = "更新客户信息档案", type = "UPDATE")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody FatManagementRecord record) {
        record.setId(id);
        return Result.success(recordMapper.updateById(record) > 0);
    }

    private Map<String, Object> metric(String label, Object value, String hint) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        item.put("hint", hint);
        return item;
    }

    private Map<String, Object> stage(String name, long value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }

    private Map<String, Object> module(String name, Object value, String hint) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        item.put("hint", hint);
        return item;
    }

    private long moduleCount(List<FatManagementRecord> records, List<Contact> contacts) {
        long modules = 0;
        if (!records.isEmpty()) modules++;
        if (!contacts.isEmpty()) modules += 5;
        return modules;
    }

    private Map<String, Object> customerRow(Contact contact) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", contact.getId());
        row.put("name", contact.getName());
        row.put("phone", contact.getPhone());
        row.put("email", contact.getEmail());
        row.put("company", contact.getCompany());
        row.put("position", contact.getPosition());
        row.put("level", contact.getLevel());
        row.put("source", contact.getSource());
        return row;
    }

    private Map<String, Object> identityRow(Contact contact) {
        Map<String, Object> row = customerRow(contact);
        row.put("identityType", "身份证");
        row.put("identityNo", "待补充");
        row.put("birthday", "待补充");
        row.put("gender", "待补充");
        return row;
    }

    private Map<String, Object> addressRow(Contact contact) {
        Map<String, Object> row = customerRow(contact);
        row.put("province", "待补充");
        row.put("city", "待补充");
        row.put("district", "待补充");
        row.put("address", "待补充详细住址");
        return row;
    }

    private Map<String, Object> relationshipRow(Contact contact) {
        Map<String, Object> row = customerRow(contact);
        row.put("node", contact.getName());
        row.put("relation", "任职");
        row.put("target", contact.getCompany() == null ? "未知企业" : contact.getCompany());
        row.put("strength", contact.getLevel() == null ? 1 : contact.getLevel());
        return row;
    }

    private Map<String, Object> websiteRow(Contact contact) {
        Map<String, Object> row = customerRow(contact);
        String domain = contact.getCompany() == null ? "example.com" : contact.getCompany().replaceAll("\\s+", "").toLowerCase() + ".com";
        row.put("website", "https://" + domain);
        row.put("blog", "https://" + domain + "/blog");
        row.put("contentPreference", contact.getRemark() == null ? "产品资讯" : contact.getRemark());
        return row;
    }

    private Map<String, Object> interactionRow(CustomerInteraction interaction) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("contactId", interaction.getContactId());
        row.put("type", interaction.getInteractionType());
        row.put("title", interaction.getTitle());
        row.put("description", interaction.getDescription());
        row.put("time", interaction.getCreateTime());
        return row;
    }

    private Map<String, Object> infoItemRow(CustomerInfoItem item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", item.getId());
        row.put("moduleItem", true);
        row.put("name", item.getCustomerName());
        row.put("customerName", item.getCustomerName());
        row.put("identityType", item.getIdentityType());
        row.put("identityNo", item.getIdentityNo());
        row.put("birthday", item.getBirthday());
        row.put("gender", item.getGender());
        row.put("email", item.getEmail());
        row.put("phone", item.getPhone());
        row.put("province", item.getProvince());
        row.put("city", item.getCity());
        row.put("district", item.getDistrict());
        row.put("address", item.getAddress());
        row.put("node", item.getCustomerName());
        row.put("relation", item.getRelation());
        row.put("target", item.getTarget());
        row.put("website", item.getWebsite());
        row.put("blog", item.getBlog());
        row.put("contentPreference", item.getContentPreference());
        row.put("type", item.getContactType());
        row.put("title", item.getTitle());
        row.put("description", item.getDescription());
        row.put("time", item.getEventTime() == null ? item.getCreateTime() : item.getEventTime());
        return row;
    }

    private long countStage(List<FatManagementRecord> records, String stage) {
        return records.stream().filter(item -> stage.equals(item.getStage())).count();
    }

    private BigDecimal average(List<BigDecimal> values) {
        List<BigDecimal> valid = values.stream().filter(v -> v != null).toList();
        if (valid.isEmpty()) return BigDecimal.ZERO.setScale(1);
        BigDecimal total = valid.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(valid.size()), 1, RoundingMode.HALF_UP);
    }
}

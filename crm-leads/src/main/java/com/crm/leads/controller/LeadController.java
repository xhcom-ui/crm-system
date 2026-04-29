package com.crm.leads.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.result.Result;
import com.crm.leads.entity.Lead;
import com.crm.leads.feign.SalesFeignClient;
import com.crm.leads.feign.dto.OpportunityDTO;
import com.crm.leads.service.LeadScoringEngine;
import com.crm.leads.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/leads/lead")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;
    private final SalesFeignClient salesFeignClient;
    private final LeadScoringEngine scoringEngine;

    @GetMapping("/page")
    @SaCheckPermission("leads:list")
    public Result<IPage<Lead>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(leadService.pageLeads(new Page<>(current, size), keyword));
    }

    @GetMapping("/{id}")
    public Result<Lead> getById(@PathVariable Long id) {
        return Result.success(leadService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("leads:add")
    public Result<Boolean> save(@Valid @RequestBody Lead lead) {
        // 保存前自动评分
        scoringEngine.scoreLead(lead);
        return Result.success(leadService.save(lead));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("leads:edit")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody Lead lead) {
        lead.setId(id);
        scoringEngine.scoreLead(lead);
        return Result.success(leadService.updateById(lead));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("leads:del")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(leadService.removeById(id));
    }

    /**
     * 手动对线索重新评分
     */
    @PostMapping("/{id}/score")
    public Result<Map<String, Object>> score(@PathVariable Long id) {
        Lead lead = leadService.getById(id);
        if (lead == null) {
            return Result.error("线索不存在");
        }
        int oldScore = lead.getScore() != null ? lead.getScore() : 0;
        scoringEngine.scoreLead(lead);
        leadService.updateById(lead);

        Map<String, Object> result = new HashMap<>();
        result.put("leadId", lead.getId());
        result.put("oldScore", oldScore);
        result.put("newScore", lead.getScore());
        result.put("isHot", lead.getScore() >= 70);
        return Result.success(result);
    }

    /**
     * 线索转化为商机（Feign 跨服务调用）
     */
    @PostMapping("/{id}/convert")
    public Result<Map<String, Object>> convertToOpportunity(@PathVariable Long id) {
        Lead lead = leadService.getById(id);
        if (lead == null) {
            return Result.error("线索不存在");
        }
        OpportunityDTO dto = new OpportunityDTO();
        dto.setName("来自线索: " + lead.getName());
        dto.setAmount(BigDecimal.ZERO);
        dto.setStage(1);
        dto.setExpectedCloseDate(LocalDate.now().plusMonths(1));
        dto.setOwnerId(lead.getAssigneeId());
        dto.setRemark("由线索 #" + lead.getId() + " 转化");

        Result<Boolean> sr = salesFeignClient.createOpportunity(dto);

        lead.setStatus(3);
        leadService.updateById(lead);

        Map<String, Object> result = new HashMap<>();
        result.put("leadId", lead.getId());
        result.put("opportunityCreated", sr != null && sr.getData() != null && sr.getData());
        return Result.success(result);
    }
}

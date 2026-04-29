package com.crm.sales.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.result.Result;
import com.crm.sales.entity.Opportunity;
import com.crm.sales.feign.CustomerFeignClient;
import com.crm.sales.feign.dto.CustomerDTO;
import com.crm.sales.service.FunnelStatsService;
import com.crm.sales.service.OpportunityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sales/opportunity")
@RequiredArgsConstructor
public class OpportunityController {

    private final OpportunityService opportunityService;
    private final CustomerFeignClient customerFeignClient;
    private final FunnelStatsService funnelStatsService;

    @GetMapping("/page")
    @SaCheckPermission("sales:list")
    public Result<IPage<Opportunity>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(opportunityService.pageOpportunities(new Page<>(current, size), keyword));
    }

    @GetMapping("/{id}")
    public Result<Opportunity> getById(@PathVariable Long id) {
        return Result.success(opportunityService.getById(id));
    }

    @PostMapping
    @SaCheckPermission("sales:add")
    public Result<Boolean> save(@Valid @RequestBody Opportunity opportunity) {
        return Result.success(opportunityService.save(opportunity));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("sales:edit")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody Opportunity opportunity) {
        opportunity.setId(id);
        return Result.success(opportunityService.updateById(opportunity));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("sales:del")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(opportunityService.removeById(id));
    }

    @GetMapping("/{id}/detail")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        Opportunity opportunity = opportunityService.getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("opportunity", opportunity);
        if (opportunity != null && opportunity.getContactId() != null) {
            try {
                Result<CustomerDTO> r = customerFeignClient.getContactById(opportunity.getContactId());
                if (r != null && r.getData() != null) {
                    result.put("customer", r.getData());
                }
            } catch (Exception e) {
                result.put("customer", null);
            }
        }
        return Result.success(result);
    }

    @GetMapping("/funnel")
    public Result<Map<String, Object>> funnel() {
        return Result.success(funnelStatsService.getFunnelData());
    }

    @GetMapping("/win-rate")
    public Result<Map<String, Object>> winRate() {
        return Result.success(funnelStatsService.getWinRate());
    }
}

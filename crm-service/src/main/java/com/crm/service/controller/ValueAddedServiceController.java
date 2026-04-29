package com.crm.service.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.log.OperationLog;
import com.crm.common.result.Result;
import com.crm.service.entity.ValueAddedService;
import com.crm.service.mapper.ValueAddedServiceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/service/value-added")
@RequiredArgsConstructor
public class ValueAddedServiceController {
    private final ValueAddedServiceMapper valueAddedServiceMapper;

    @GetMapping("/page")
    @SaCheckPermission("value-service:list")
    public Result<IPage<ValueAddedService>> page(@RequestParam(defaultValue = "1") Integer current,
                                                 @RequestParam(defaultValue = "10") Integer size,
                                                 @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<ValueAddedService> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(ValueAddedService::getName, keyword)
                    .or().like(ValueAddedService::getCustomerName, keyword)
                    .or().like(ValueAddedService::getOwnerName, keyword));
        }
        wrapper.orderByAsc(ValueAddedService::getExpireDate).orderByDesc(ValueAddedService::getCreateTime);
        return Result.success(valueAddedServiceMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @PostMapping
    @SaCheckPermission("value-service:add")
    @OperationLog(title = "新增增值服务", type = "INSERT")
    public Result<Boolean> save(@RequestBody ValueAddedService service) {
        return Result.success(valueAddedServiceMapper.insert(service) > 0);
    }

    @GetMapping("/summary")
    public Result<List<Map<String, Object>>> summary() {
        List<ValueAddedService> rows = valueAddedServiceMapper.selectList(new LambdaQueryWrapper<>());
        long active = rows.stream().filter(row -> row.getStatus() != null && row.getStatus() == 1).count();
        long expiring = rows.stream()
                .filter(row -> row.getExpireDate() != null && !row.getExpireDate().isAfter(LocalDate.now().plusDays(30)))
                .count();
        return Result.success(List.of(
                item("服务中", active, "当前有效服务"),
                item("即将到期", expiring, "30 天内到期"),
                item("本月续费", "¥86,000", "已确认金额"),
                item("待回访", 9, "需客户成功跟进")
        ));
    }

    private Map<String, Object> item(String label, Object value, String hint) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        item.put("hint", hint);
        return item;
    }
}

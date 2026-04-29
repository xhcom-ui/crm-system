package com.crm.customer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.log.OperationLog;
import com.crm.common.result.Result;
import com.crm.customer.entity.CustomerInteraction;
import com.crm.customer.mapper.CustomerInteractionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer/interaction")
@RequiredArgsConstructor
public class CustomerInteractionController {

    private final CustomerInteractionMapper interactionMapper;

    @GetMapping("/list/{contactId}")
    public Result<List<CustomerInteraction>> listByContact(@PathVariable Long contactId) {
        return Result.success(interactionMapper.selectList(
                new LambdaQueryWrapper<CustomerInteraction>()
                        .eq(CustomerInteraction::getContactId, contactId)
                        .orderByDesc(CustomerInteraction::getCreateTime)));
    }

    @PostMapping
    @SaCheckPermission("customer:interaction:add")
    @OperationLog(title = "新增客户互动", type = "INSERT")
    public Result<Boolean> save(@RequestBody CustomerInteraction interaction) {
        return Result.success(interactionMapper.insert(interaction) > 0);
    }
}

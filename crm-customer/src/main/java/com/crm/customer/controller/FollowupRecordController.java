package com.crm.customer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.log.OperationLog;
import com.crm.common.result.Result;
import com.crm.customer.entity.FollowupRecord;
import com.crm.customer.mapper.FollowupRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer/followup")
@RequiredArgsConstructor
public class FollowupRecordController {
    private final FollowupRecordMapper followupRecordMapper;

    @GetMapping("/page")
    @SaCheckPermission("followup:list")
    public Result<IPage<FollowupRecord>> page(@RequestParam(defaultValue = "1") Integer current,
                                              @RequestParam(defaultValue = "10") Integer size,
                                              @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<FollowupRecord> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(FollowupRecord::getCustomerName, keyword)
                    .or().like(FollowupRecord::getTitle, keyword)
                    .or().like(FollowupRecord::getContent, keyword)
                    .or().like(FollowupRecord::getOwnerName, keyword));
        }
        wrapper.orderByDesc(FollowupRecord::getFollowTime).orderByDesc(FollowupRecord::getCreateTime);
        return Result.success(followupRecordMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @PostMapping
    @SaCheckPermission("followup:add")
    @OperationLog(title = "新增跟进记录", type = "INSERT")
    public Result<Boolean> save(@RequestBody FollowupRecord record) {
        return Result.success(followupRecordMapper.insert(record) > 0);
    }
}

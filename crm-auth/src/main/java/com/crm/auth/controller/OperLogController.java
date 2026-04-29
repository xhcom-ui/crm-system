package com.crm.auth.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.auth.entity.OperLog;
import com.crm.auth.mapper.OperLogMapper;
import com.crm.common.log.OperationLogRecord;
import com.crm.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志 Controller
 */
@RestController
@RequestMapping("/system/oper-log")
@RequiredArgsConstructor
public class OperLogController {

    private final OperLogMapper operLogMapper;

    /**
     * 操作日志列表（分页）
     */
    @GetMapping("/page")
    @SaCheckPermission("system:operlog:list")
    public Result<Page<OperLog>> page(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String title,
                                       @RequestParam(required = false) String operName) {
        LambdaQueryWrapper<OperLog> wrapper = new LambdaQueryWrapper<OperLog>()
                .like(title != null && !title.isEmpty(), OperLog::getTitle, title)
                .like(operName != null && !operName.isEmpty(), OperLog::getOperName, operName)
                .orderByDesc(OperLog::getCreateTime);
        return Result.success(operLogMapper.selectPage(new Page<>(current, size), wrapper));
    }

    /**
     * 清空日志
     */
    @DeleteMapping("/clean")
    @SaCheckPermission("system:operlog:del")
    public Result<Void> clean() {
        operLogMapper.delete(new LambdaQueryWrapper<>());
        return Result.success();
    }

    /**
     * 微服务内部操作日志采集接口。
     */
    @PostMapping("/internal")
    public Result<Void> internal(@RequestBody OperationLogRecord record) {
        OperLog log = new OperLog();
        log.setTitle(record.getTitle());
        log.setOperType(record.getOperType());
        log.setOperName(record.getOperName());
        log.setRequestUrl(record.getRequestUrl());
        log.setRequestMethod(record.getRequestMethod());
        log.setRequestParams(record.getRequestParams());
        log.setJsonResult(record.getJsonResult());
        log.setStatus(record.getStatus());
        log.setErrorMsg(record.getErrorMsg());
        log.setCostTime(record.getCostTime());
        log.setOperIp(record.getOperIp());
        operLogMapper.insert(log);
        return Result.success();
    }
}

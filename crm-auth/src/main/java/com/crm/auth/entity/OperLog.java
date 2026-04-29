package com.crm.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志
 */
@Data
@TableName("sys_oper_log")
public class OperLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作模块 */
    private String title;

    /** 操作类型 */
    private String operType;

    /** 操作人员 */
    private String operName;

    /** 请求URL */
    private String requestUrl;

    /** 请求方式 */
    private String requestMethod;

    /** 请求参数 */
    private String requestParams;

    /** 返回结果 */
    private String jsonResult;

    /** 操作状态 (0-失败, 1-成功) */
    private Integer status;

    /** 错误消息 */
    private String errorMsg;

    /** 耗时(毫秒) */
    private Long costTime;

    /** 操作IP */
    private String operIp;

    /** 操作时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

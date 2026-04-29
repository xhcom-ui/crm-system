package com.crm.common.log;

import lombok.Data;

@Data
public class OperationLogRecord {
    private String title;
    private String operType;
    private String operName;
    private String requestUrl;
    private String requestMethod;
    private String requestParams;
    private String jsonResult;
    private Integer status;
    private String errorMsg;
    private Long costTime;
    private String operIp;
}

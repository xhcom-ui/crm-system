package com.crm.common.result;

/**
 * 统一响应状态码
 */
public interface ResultCode {

    int SUCCESS = 200;
    int BAD_REQUEST = 400;
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int INTERNAL_ERROR = 500;

    String SUCCESS_MSG = "操作成功";
    String ERROR_MSG = "系统异常";
}

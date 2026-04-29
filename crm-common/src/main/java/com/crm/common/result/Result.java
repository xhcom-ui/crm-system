package com.crm.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应体
 *
 * @param <T> 数据类型
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int code;
    private String message;
    private T data;
    private long timestamp;

    private Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS;
        result.message = ResultCode.SUCCESS_MSG;
        result.data = data;
        return result;
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS;
        result.message = message;
        result.data = data;
        return result;
    }

    public static <T> Result<T> error() {
        return error(ResultCode.INTERNAL_ERROR, ResultCode.ERROR_MSG);
    }

    public static <T> Result<T> error(String message) {
        return error(ResultCode.INTERNAL_ERROR, message);
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        return result;
    }
}

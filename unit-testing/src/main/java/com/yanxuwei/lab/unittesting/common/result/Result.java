package com.yanxuwei.lab.unittesting.common.result;

/**
 * 接口统一响应，包含状态码、消息和响应数据。
 *
 * @param <T> 响应数据类型
 * @version 1.0
 * @since 2026/11/18
 */
public class Result<T> {
    private final int code;
    private final String message;
    private final T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(ResultCode.SUCCESS.code(), ResultCode.SUCCESS.message(), data);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    public int getCode() { return code; }

    public String getMessage() { return message; }

    public T getData() { return data; }
}

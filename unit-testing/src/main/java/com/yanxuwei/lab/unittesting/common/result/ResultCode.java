package com.yanxuwei.lab.unittesting.common.result;

/**
 * 统一响应使用的状态码和默认消息。
 *
 * @version 1.0
 * @since 2026/11/18
 */
public enum ResultCode {
    SUCCESS(200, "成功"),
    PARAM_ERROR(400, "参数错误");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() { return code; }

    public String message() { return message; }
}

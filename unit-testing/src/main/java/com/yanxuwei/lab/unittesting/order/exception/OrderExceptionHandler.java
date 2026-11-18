package com.yanxuwei.lab.unittesting.order.exception;

import com.yanxuwei.lab.unittesting.common.result.Result;
import com.yanxuwei.lab.unittesting.common.result.ResultCode;
import com.yanxuwei.lab.unittesting.order.controller.OrderController;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 订单接口异常处理器，将业务异常和请求体解析异常转换为统一响应。
 * 处理范围仅限订单 Controller。
 *
 * @version 1.0
 * @since 2026/11/18
 */
@RestControllerAdvice(assignableTypes = OrderController.class)
public class OrderExceptionHandler {
    @ExceptionHandler(OrderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleOrderException(OrderException exception) {
        return Result.error(ResultCode.PARAM_ERROR.code(), exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleInvalidBody(HttpMessageNotReadableException exception) {
        return Result.error(ResultCode.PARAM_ERROR.code(), "请求体缺失或格式错误");
    }
}

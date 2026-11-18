package com.yanxuwei.lab.unittesting.order.exception;

/**
 * 订单报价参数不符合业务规则时抛出的异常。
 *
 * @version 1.0
 * @since 2026/11/18
 */
public class OrderException extends RuntimeException {
    public OrderException(String message) {
        super(message);
    }
}

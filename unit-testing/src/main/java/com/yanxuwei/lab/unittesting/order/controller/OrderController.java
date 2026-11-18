package com.yanxuwei.lab.unittesting.order.controller;

import com.yanxuwei.lab.unittesting.common.result.Result;
import com.yanxuwei.lab.unittesting.order.domain.OrderQuote;
import com.yanxuwei.lab.unittesting.order.domain.OrderRequest;
import com.yanxuwei.lab.unittesting.order.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单报价访问层，接收报价请求并返回统一响应。
 *
 * @version 1.0
 * @since 2026/11/18
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/quote")
    public Result<OrderQuote> calculateQuote(@RequestBody OrderRequest request) {
        return Result.ok(orderService.calculateQuote(request));
    }
}

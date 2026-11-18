package com.yanxuwei.lab.unittesting.order;

import com.yanxuwei.lab.unittesting.order.controller.OrderController;
import com.yanxuwei.lab.unittesting.order.domain.OrderQuote;
import com.yanxuwei.lab.unittesting.order.domain.OrderRequest;
import com.yanxuwei.lab.unittesting.order.exception.OrderException;
import com.yanxuwei.lab.unittesting.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 订单报价访问层单元测试类
 *
 * @author yxw
 * @version 1.0
 * @since 2026/11/18
 */
@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("报价请求有效时，返回报价并正确传递参数")
    void calculateQuote_whenRequestIsValid_shouldReturnQuote() throws Exception {
        // 准备：模拟 Service 返回的结果。
        OrderQuote quote = new OrderQuote(new BigDecimal("200.00"), new BigDecimal("10.00"),
                new BigDecimal("0.00"), new BigDecimal("190.00"));
        when(orderService.calculateQuote(any(OrderRequest.class))).thenReturn(quote);

        // 执行并断言：发送 JSON 请求，检查 HTTP 状态和响应内容。
        mockMvc.perform(post("/api/v1/orders/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"unitPrice\": 20, \"quantity\": 10}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"code\": 200, \"message\": \"成功\", \"data\": {\"goodsAmount\": 200.00, \"discountAmount\": 10.00, \"shippingFee\": 0.00, \"payableAmount\": 190.00}}"));

        // 检查 Controller 传给 Service 参数。
        verify(orderService).calculateQuote(argThat(request -> request != null
                && request.getUnitPrice() != null
                && request.getUnitPrice().compareTo(new BigDecimal("20")) == 0
                && Integer.valueOf(10).equals(request.getQuantity())));
    }

    @Test
    @DisplayName("Service 抛出订单异常时，返回统一错误响应")
    void calculateQuote_whenServiceThrowsOrderException_shouldReturnErrorResponse() throws Exception {
        // 准备：模拟 Service 返回的结果。
        when(orderService.calculateQuote(any(OrderRequest.class)))
                .thenThrow(new OrderException("商品单价必须大于 0，且最多保留两位小数"));

        // 执行并断言：发送 JSON 请求，检查 HTTP 状态和响应内容。
        mockMvc.perform(post("/api/v1/orders/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"unitPrice\": 0, \"quantity\": 10}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"code\": 400, \"message\": \"商品单价必须大于 0，且最多保留两位小数\", \"data\": null}"));

        // 检查 Controller 传给 Service 参数。
        verify(orderService).calculateQuote(argThat(request -> request != null
                && request.getUnitPrice() != null
                && request.getUnitPrice().compareTo(new BigDecimal("0")) == 0
                && Integer.valueOf(10).equals(request.getQuantity())));
    }

    @Test
    @DisplayName("请求体缺失时，返回统一错误响应")
    void calculateQuote_whenBodyIsMissing_shouldReturnErrorResponse() throws Exception {
        mockMvc.perform(post("/api/v1/orders/quote")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"code\": 400, \"message\": \"请求体缺失或格式错误\", \"data\": null}"));

        // 请求未通过解析，不应调用 Service。
        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("请求体不是合法 JSON 时，返回统一错误响应")
    void calculateQuote_whenJsonIsMalformed_shouldReturnErrorResponse() throws Exception {
        mockMvc.perform(post("/api/v1/orders/quote")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"unitPrice\": }"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"code\": 400, \"message\": \"请求体缺失或格式错误\", \"data\": null}"));

        // 请求未通过解析，不应调用 Service。
        verifyNoInteractions(orderService);
    }
}

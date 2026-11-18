package com.yanxuwei.lab.unittesting.order;

import com.yanxuwei.lab.unittesting.order.domain.OrderQuote;
import com.yanxuwei.lab.unittesting.order.domain.OrderRequest;
import com.yanxuwei.lab.unittesting.order.exception.OrderException;
import com.yanxuwei.lab.unittesting.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * 订单报价业务层单元测试类
 *
 * @author yxw
 * @version 1.0
 * @since 2026/11/18
 */
public class OrderServiceTest {

    @Test
    @DisplayName("商品总价为 99.99 元时，收取运费")
    void calculateQuote_whenGoodsAmountIsBelow100_shouldChargeShipping() {
        // 准备：创建服务和订单请求，商品总价为 99.99 元
        OrderRequest orderRequest = new OrderRequest(new BigDecimal("11.11"), 9);
        OrderService orderService = new OrderService();

        // 执行：调用 calculateQuote
        OrderQuote orderQuote = orderService.calculateQuote(orderRequest);

        // 断言：检查返回结果商品总价、折扣金额、运费、支付金额与预期保持一致
        assertEquals(new BigDecimal("99.99"), orderQuote.getGoodsAmount());
        assertEquals(new BigDecimal("0.00"), orderQuote.getDiscountAmount());
        assertEquals(new BigDecimal("10.00"), orderQuote.getShippingFee());
        assertEquals(new BigDecimal("109.99"), orderQuote.getPayableAmount());
    }

    @Test
    @DisplayName("商品总价为 100 元时，刚好免运费")
    void calculateQuote_whenGoodsAmountIs100_shouldProvideFreeShipping() {
        // 准备：创建服务和订单请求，商品总价为 100 元
        OrderRequest orderRequest = new OrderRequest(new BigDecimal("20"), 5);
        OrderService orderService = new OrderService();

        // 执行：调用 calculateQuote
        OrderQuote orderQuote = orderService.calculateQuote(orderRequest);

        // 断言：检查返回结果商品总价、折扣金额、运费、支付金额与预期保持一致
        assertEquals(new BigDecimal("100.00"), orderQuote.getGoodsAmount());
        assertEquals(new BigDecimal("0.00"), orderQuote.getDiscountAmount());
        assertEquals(new BigDecimal("0.00"), orderQuote.getShippingFee());
        assertEquals(new BigDecimal("100.00"), orderQuote.getPayableAmount());
    }

    @Test
    @DisplayName("商品总价为 199.99 元时，不享受折扣")
    void calculateQuote_whenGoodsAmountIsBelow200_shouldApplyNoDiscount() {
        OrderRequest request = new OrderRequest(new BigDecimal("199.99"), 1);
        OrderService service = new OrderService();

        OrderQuote quote = service.calculateQuote(request);

        assertEquals(new BigDecimal("199.99"), quote.getGoodsAmount());
        assertEquals(new BigDecimal("0.00"), quote.getDiscountAmount());
        assertEquals(new BigDecimal("0.00"), quote.getShippingFee());
        assertEquals(new BigDecimal("199.99"), quote.getPayableAmount());
    }

    @Test
    @DisplayName("商品总价为 200 元时，优惠 5% 且免运费")
    void calculateQuote_whenGoodsAmountIs200_shouldApplyFivePercentDiscount() {
        // 准备：创建服务和订单请求，商品总价为 200 元
        OrderRequest orderRequest = new OrderRequest(new BigDecimal("20"), 10);
        OrderService orderService = new OrderService();

        // 执行：调用 calculateQuote
        OrderQuote orderQuote = orderService.calculateQuote(orderRequest);

        // 断言：检查返回结果商品总价、折扣金额、运费、支付金额与预期保持一致
        assertEquals(new BigDecimal("200.00"), orderQuote.getGoodsAmount());
        assertEquals(new BigDecimal("10.00"), orderQuote.getDiscountAmount());
        assertEquals(new BigDecimal("0.00"), orderQuote.getShippingFee());
        assertEquals(new BigDecimal("190.00"), orderQuote.getPayableAmount());
    }

    @Test
    @DisplayName("商品总价为 499.99 元时，优惠 5% 并四舍五入")
    void calculateQuote_whenGoodsAmountIsBelow500_shouldApplyFivePercentDiscount() {
        OrderRequest request = new OrderRequest(new BigDecimal("499.99"), 1);
        OrderService service = new OrderService();

        OrderQuote quote = service.calculateQuote(request);

        assertEquals(new BigDecimal("499.99"), quote.getGoodsAmount());
        assertEquals(new BigDecimal("25.00"), quote.getDiscountAmount());
        assertEquals(new BigDecimal("0.00"), quote.getShippingFee());
        assertEquals(new BigDecimal("474.99"), quote.getPayableAmount());
    }

    @Test
    @DisplayName("商品总价为 500 元时，优惠 10%")
    void calculateQuote_whenGoodsAmountIs500_shouldApplyTenPercentDiscount() {
        OrderRequest request = new OrderRequest(new BigDecimal("100.00"), 5);
        OrderService service = new OrderService();

        OrderQuote quote = service.calculateQuote(request);

        assertEquals(new BigDecimal("500.00"), quote.getGoodsAmount());
        assertEquals(new BigDecimal("50.00"), quote.getDiscountAmount());
        assertEquals(new BigDecimal("0.00"), quote.getShippingFee());
        assertEquals(new BigDecimal("450.00"), quote.getPayableAmount());
    }

    @Test
    @DisplayName("订单请求为空时，抛出订单异常")
    void calculateQuote_whenRequestIsNull_shouldThrowOrderException() {
        OrderService service = new OrderService();

        OrderException exception = assertThrows(OrderException.class,
                () -> service.calculateQuote(null));

        assertEquals("订单请求不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("商品单价为 0 时，抛出订单异常")
    void calculateQuote_whenPriceIsZero_shouldThrowOrderException() {
        OrderRequest request = new OrderRequest(BigDecimal.ZERO, 1);
        OrderService service = new OrderService();

        OrderException exception = assertThrows(OrderException.class,
                () -> service.calculateQuote(request));

        assertEquals("商品单价必须大于 0，且最多保留两位小数", exception.getMessage());
    }

    @Test
    @DisplayName("商品数量大于 100 时，抛出订单异常")
    void calculateQuote_whenQuantityExceeds100_shouldThrowOrderException() {
        OrderRequest request = new OrderRequest(new BigDecimal("10.00"), 101);
        OrderService service = new OrderService();

        OrderException exception = assertThrows(OrderException.class,
                () -> service.calculateQuote(request));

        assertEquals("商品数量必须在 1 到 100 之间", exception.getMessage());
    }

    @Test
    @DisplayName("商品数量为 100 时，可以计算报价")
    void calculateQuote_whenQuantityIs100_shouldReturnQuote() {
        OrderRequest request = new OrderRequest(new BigDecimal("1.00"), 100);
        OrderQuote quote = new OrderService().calculateQuote(request);

        assertEquals(new BigDecimal("100.00"), quote.getGoodsAmount());
        assertEquals(new BigDecimal("100.00"), quote.getPayableAmount());
    }

    @Test
    @DisplayName("商品单价为空时，抛出订单异常")
    void calculateQuote_whenPriceIsNull_shouldThrowOrderException() {
        OrderRequest request = new OrderRequest(null, 1);
        OrderException exception = assertThrows(OrderException.class,
                () -> new OrderService().calculateQuote(request));

        assertEquals("商品单价必须大于 0，且最多保留两位小数", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "1.001"})
    @DisplayName("商品单价为负数或超过两位小数时，抛出订单异常")
    void calculateQuote_whenPriceIsInvalid_shouldThrowOrderException(String price) {
        OrderRequest request = new OrderRequest(new BigDecimal(price), 1);
        OrderException exception = assertThrows(OrderException.class,
                () -> new OrderService().calculateQuote(request));

        assertEquals("商品单价必须大于 0，且最多保留两位小数", exception.getMessage());
    }

    @Test
    @DisplayName("商品数量为 0 时，抛出订单异常")
    void calculateQuote_whenQuantityIsZero_shouldThrowOrderException() {
        OrderRequest request = new OrderRequest(new BigDecimal("10.00"), 0);
        OrderException exception = assertThrows(OrderException.class,
                () -> new OrderService().calculateQuote(request));

        assertEquals("商品数量必须在 1 到 100 之间", exception.getMessage());
    }

    @Test
    @DisplayName("商品数量为空时，抛出订单异常")
    void calculateQuote_whenQuantityIsNull_shouldThrowOrderException() {
        OrderRequest request = new OrderRequest(new BigDecimal("10.00"), null);
        OrderException exception = assertThrows(OrderException.class, () -> new OrderService().calculateQuote(request));
        assertEquals("商品数量必须在 1 到 100 之间", exception.getMessage());
    }

    @Test
    @DisplayName("优惠金额恰好有半分时，四舍五入到分")
    void calculateQuote_whenDiscountHasHalfCent_shouldRoundUp() {
        OrderRequest request = new OrderRequest(new BigDecimal("200.10"), 1);

        OrderQuote quote = new OrderService().calculateQuote(request);

        assertEquals(new BigDecimal("200.10"), quote.getGoodsAmount());
        assertEquals(new BigDecimal("10.01"), quote.getDiscountAmount());
        assertEquals(new BigDecimal("0.00"), quote.getShippingFee());
        assertEquals(new BigDecimal("190.09"), quote.getPayableAmount());
    }

    @Test
    @DisplayName("单价和数量都无效时，先返回单价错误")
    void calculateQuote_whenPriceAndQuantityAreInvalid_shouldReportPriceErrorFirst() {
        OrderRequest request = new OrderRequest(BigDecimal.ZERO, 0);

        OrderException exception = assertThrows(OrderException.class,
                () -> new OrderService().calculateQuote(request));

        assertEquals("商品单价必须大于 0，且最多保留两位小数", exception.getMessage());
    }
}

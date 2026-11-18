package com.yanxuwei.lab.unittesting.order.service;

import com.yanxuwei.lab.unittesting.order.domain.OrderQuote;
import com.yanxuwei.lab.unittesting.order.domain.OrderRequest;
import com.yanxuwei.lab.unittesting.order.exception.OrderException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 订单报价业务层，计算商品总价、优惠、运费和应付金额。
 *
 * @version 1.0
 * @since 2026/11/18
 */
@Service
public class OrderService {

        public OrderQuote calculateQuote(OrderRequest request) {

        // 先校验请求对象，避免获取字段时出现空指针。
        if (request == null) {
            throw new OrderException("订单请求不能为空");
        }
        BigDecimal price = request.getUnitPrice();
        Integer quantity = request.getQuantity();
        // 单价必须大于 0，且最多保留两位小数；数量限制为 1～100。
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0 || price.scale() > 2) {
            throw new OrderException("商品单价必须大于 0，且最多保留两位小数");
        }
        if (quantity == null || quantity < 1 || quantity > 100) {
            throw new OrderException("商品数量必须在 1 到 100 之间");
        }

        // 商品总价 = 单价 × 数量；单价最多两位小数，乘以整数后无须舍入。
        BigDecimal goodsAmount = price.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.UNNECESSARY);
        BigDecimal discountRate = BigDecimal.ZERO;
        // 按折扣前总价判断优惠；先判断较高门槛。
        if (goodsAmount.compareTo(new BigDecimal("500")) >= 0) {
            discountRate = new BigDecimal("0.10");
        } else if (goodsAmount.compareTo(new BigDecimal("200")) >= 0) {
            discountRate = new BigDecimal("0.05");
        }
        // 优惠金额四舍五入到分；运费门槛也按折扣前总价判断。
        BigDecimal discount = goodsAmount.multiply(discountRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal shipping = goodsAmount.compareTo(new BigDecimal("100")) >= 0
                ? new BigDecimal("0.00") : new BigDecimal("10.00");
        // 应付金额 = 商品总价 - 优惠金额 + 运费。
        BigDecimal payable = goodsAmount.subtract(discount).add(shipping);
        return new OrderQuote(goodsAmount, discount, shipping, payable);
    }
}

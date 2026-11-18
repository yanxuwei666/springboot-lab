package com.yanxuwei.lab.unittesting.order.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 订单报价结果，金额以元为单位，保留两位小数。
 *
 * @version 1.0
 * @since 2026/11/18
 */
@Data
@AllArgsConstructor
public class OrderQuote {
    private BigDecimal goodsAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal payableAmount;
}

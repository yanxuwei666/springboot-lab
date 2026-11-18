package com.yanxuwei.lab.unittesting.order.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单报价请求，包含商品单价和购买数量。
 *
 * @version 1.0
 * @since 2026/11/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private BigDecimal unitPrice;
    private Integer quantity;
}

package com.yanxuwei.lab.unittesting.amount.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 请假实体类
 *
 * @author yxw
 * @version 1.0
 * @since 2026/11/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LeaveApply {

    /** 请假天数，按整数计算。 */
    private Integer days;
}

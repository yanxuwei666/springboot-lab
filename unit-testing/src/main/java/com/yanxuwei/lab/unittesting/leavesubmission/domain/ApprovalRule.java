package com.yanxuwei.lab.unittesting.leavesubmission.domain;

import lombok.Data;

/** 简化的审批规则：只记录命中的规则和处理方式。 */
@Data
public class ApprovalRule {
    private Long ruleId;
    private String submitMode;
}

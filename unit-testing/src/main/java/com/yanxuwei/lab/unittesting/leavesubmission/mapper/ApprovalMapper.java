package com.yanxuwei.lab.unittesting.leavesubmission.mapper;

import com.yanxuwei.lab.unittesting.leavesubmission.domain.ApprovalRule;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;

/** 练习用的模拟审批规则和节点访问层。 */
@Repository
public class ApprovalMapper {
    /** 默认返回一条需要审批的规则。 */
    public ApprovalRule findRule(String leaveType, BigDecimal duration) {
        ApprovalRule rule = new ApprovalRule();
        rule.setRuleId(1L);
        rule.setSubmitMode("APPROVAL");
        return rule;
    }

    /** 模拟创建首个审批节点，不保存数据。 */
    public int insertFirstNode(Long requestId) { return 1; }
}

package com.yanxuwei.lab.unittesting.amount.service;

import com.yanxuwei.lab.unittesting.amount.domain.LeaveApply;
import org.springframework.stereotype.Service;

/**
 * 请假业务层
 *
 * @author yxw
 * @version 1.0
 * @since 2026/11/18
 */
@Service
public class LeaveService {

    public String submitLeaveApply(LeaveApply leaveApply) {
        Integer days = leaveApply == null ? null : leaveApply.getDays();
        if (days == null || days < 1 || days > 5) {
            return "请假天数必须在 1 到 5 天之间";
        }
        return "申请成功";
    }
}

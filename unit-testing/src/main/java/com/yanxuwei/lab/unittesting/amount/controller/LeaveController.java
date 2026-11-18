package com.yanxuwei.lab.unittesting.amount.controller;

import com.yanxuwei.lab.unittesting.amount.domain.LeaveApply;
import com.yanxuwei.lab.unittesting.amount.service.LeaveService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 请假访问层
 *
 * @author yxw
 * @version 1.0
 * @since 2026/11/18
 */
@RequestMapping("/api/v1/leave")
@RestController
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @PostMapping("/apply")
    public String submitLeaveApply(@RequestBody LeaveApply leaveApply) {
        return leaveService.submitLeaveApply(leaveApply);
    }
}

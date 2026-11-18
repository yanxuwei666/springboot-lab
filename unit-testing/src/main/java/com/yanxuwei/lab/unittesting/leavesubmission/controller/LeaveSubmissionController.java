package com.yanxuwei.lab.unittesting.leavesubmission.controller;

import com.yanxuwei.lab.unittesting.leavesubmission.domain.LeaveRequest;
import com.yanxuwei.lab.unittesting.leavesubmission.domain.LeaveRequestDto;
import com.yanxuwei.lab.unittesting.common.result.Result;
import com.yanxuwei.lab.unittesting.leavesubmission.service.LeaveRequestService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leave/requests")
public class LeaveSubmissionController {
    private final LeaveRequestService service;

    public LeaveSubmissionController(LeaveRequestService service) {
        this.service = service;
    }

    @PostMapping("/submit")
    public Result<LeaveRequest> submit(@RequestBody LeaveRequestDto request) {
        return Result.ok(service.submitLeaveRequest(request));
    }
}

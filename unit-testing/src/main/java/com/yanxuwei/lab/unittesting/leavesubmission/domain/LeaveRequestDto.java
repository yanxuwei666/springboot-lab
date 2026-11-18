package com.yanxuwei.lab.unittesting.leavesubmission.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 请假提交表单；申请 ID 为空表示新建并提交。 */
@Data
public class LeaveRequestDto {
    private Long requestId;
    private String applicantId;
    private String leaveType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private BigDecimal duration;
    private String reason;
    private String applicantMobile;
    private String destination;
}

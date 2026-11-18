package com.yanxuwei.lab.unittesting.leavesubmission.domain;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 请假草稿及其提交后的状态；时长单位为天。 */
@Data
public class LeaveRequest {
    public static final int DRAFT = 0;
    public static final int APPROVING = 1;
    public static final int APPROVED = 2;
    public static final int REJECTED = 3;
    public static final int WITHDRAWN = 4;

    private Long requestId;
    private String applicantId;
    private String leaveType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal duration;
    private String reason;
    private String applicantMobile;
    private String destination;
    private Integer overallStatus;
    private Long matchedRuleId;
    private Integer currentNodeOrder;
}

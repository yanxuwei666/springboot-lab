package com.yanxuwei.lab.unittesting.leavesubmission.mapper;

import com.yanxuwei.lab.unittesting.leavesubmission.domain.LeaveRequest;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

/** 练习用的模拟数据访问层，不连接数据库。 */
@Repository
public class LeaveRequestMapper {
    private final AtomicLong nextId = new AtomicLong(100);

    /** 模拟保存草稿并生成 ID，不保存数据。 */
    public int insert(LeaveRequest request) {
        request.setRequestId(nextId.getAndIncrement());
        return 1;
    }

    /** ID 为 1 时返回示例草稿，其他 ID 视为不存在。 */
    public LeaveRequest getById(Long id) {
        if (!Long.valueOf(1L).equals(id)) return null;
        LeaveRequest request = new LeaveRequest();
        request.setRequestId(1L);
        request.setApplicantId("user-1");
        request.setLeaveType("事假");
        request.setStartTime(LocalDateTime.of(2027, 1, 11, 9, 0));
        request.setEndTime(LocalDateTime.of(2027, 1, 11, 17, 0));
        request.setDuration(new BigDecimal("1.0"));
        request.setReason("个人事务");
        request.setApplicantMobile("13800000000");
        request.setDestination("本市");
        request.setOverallStatus(LeaveRequest.DRAFT);
        return request;
    }

    /** 默认不存在冲突；单元测试可模拟其他结果。 */
    public int countConflicts(LeaveRequest request) { return 0; }

    /** 模拟更新成功，不保存数据。 */
    public int updateSubmission(LeaveRequest request) { return 1; }
}

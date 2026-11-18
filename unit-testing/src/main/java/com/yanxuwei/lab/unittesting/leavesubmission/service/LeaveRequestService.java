package com.yanxuwei.lab.unittesting.leavesubmission.service;

import com.yanxuwei.lab.unittesting.leavesubmission.domain.ApprovalRule;
import com.yanxuwei.lab.unittesting.leavesubmission.domain.LeaveRequest;
import com.yanxuwei.lab.unittesting.leavesubmission.domain.LeaveRequestDto;
import com.yanxuwei.lab.unittesting.leavesubmission.mapper.ApprovalMapper;
import com.yanxuwei.lab.unittesting.leavesubmission.mapper.LeaveRequestMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

/**
 * 请假申请业务层，处理新建申请和已有申请的提交。
 */
@Service
public class LeaveRequestService {
    private final LeaveRequestMapper requestMapper;
    private final ApprovalMapper approvalMapper;

    public LeaveRequestService(LeaveRequestMapper requestMapper, ApprovalMapper approvalMapper) {
        this.requestMapper = requestMapper;
        this.approvalMapper = approvalMapper;
    }

    /**
     * 提交请假申请：校验申请信息和时间冲突，匹配规则并更新状态。
     * 练习中的 Mapper 返回模拟数据，不会保存修改。
     *
     * @param form 请假申请表单；ID 为空时新建并提交
     * @return 提交后的请假申请
     */
    public LeaveRequest submitLeaveRequest(LeaveRequestDto form) {
        // 1. 新建申请或查询已有申请；已有申请需确认状态允许提交。
        if (form == null) {
            throw new IllegalArgumentException("请假申请不能为空");
        }
        boolean newRequest = form.getRequestId() == null;
        LeaveRequest request;
        if (newRequest) {
            request = new LeaveRequest();
            request.setOverallStatus(LeaveRequest.DRAFT);
        } else {
            request = requestMapper.getById(form.getRequestId());
            if (request == null) {
                throw new IllegalArgumentException("请假申请不存在");
            }
            Integer status = request.getOverallStatus();
            if (!Integer.valueOf(LeaveRequest.DRAFT).equals(status)
                    && !Integer.valueOf(LeaveRequest.REJECTED).equals(status)
                    && !Integer.valueOf(LeaveRequest.WITHDRAWN).equals(status)) {
                throw new IllegalArgumentException("当前请假申请不可重复提交");
            }
        }

        // 新建时使用表单内容；提交已有草稿时，传入请假类型表示更新表单内容。
        if (newRequest || StringUtils.hasText(form.getLeaveType())) {
            request.setApplicantId(form.getApplicantId());
            request.setLeaveType(form.getLeaveType());
            request.setStartTime(form.getStartTime());
            request.setEndTime(form.getEndTime());
            request.setDuration(form.getDuration());
            request.setReason(form.getReason());
            request.setApplicantMobile(form.getApplicantMobile());
            request.setDestination(form.getDestination());
        }

        // 2. 检查申请信息是否填写完整。
        if (!StringUtils.hasText(request.getApplicantId())
                || !StringUtils.hasText(request.getLeaveType())
                || request.getStartTime() == null
                || request.getEndTime() == null
                || request.getDuration() == null
                || !StringUtils.hasText(request.getReason())
                || !StringUtils.hasText(request.getApplicantMobile())
                || !StringUtils.hasText(request.getDestination())) {
            throw new IllegalArgumentException("请完整填写请假信息");
        }

        // 3. 检查时间和时长；年休假还必须按整天申请。
        BigDecimal duration = request.getDuration();
        if (!request.getStartTime().isBefore(request.getEndTime())
                || duration.compareTo(BigDecimal.ZERO) <= 0
                || duration.stripTrailingZeros().scale() > 1) {
            throw new IllegalArgumentException("请假时间或时长不合法");
        }

        if ("年休假".equals(request.getLeaveType()) && duration.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("年休假时长必须为整天");
        }

        // 4. 检查同一申请人的请假时间是否冲突。
        if (requestMapper.countConflicts(request) > 0) {
            throw new IllegalArgumentException("该请假时间与已有请假申请冲突");
        }

        // 5. 匹配审批规则，确定提交后如何流转。
        ApprovalRule rule = approvalMapper.findRule(request.getLeaveType(), duration);
        if (rule == null) {
            throw new IllegalArgumentException("未匹配到可用的审批规则");
        }
        if (!"AUTO_COMPLETE".equals(rule.getSubmitMode()) && !"APPROVAL".equals(rule.getSubmitMode())) {
            throw new IllegalArgumentException("审批规则处理方式无效");
        }

        // 新建申请先模拟保存并取得 ID，再创建审批节点。
        if (newRequest) {
            requestMapper.insert(request);
        }
        request.setMatchedRuleId(rule.getRuleId());

        if ("AUTO_COMPLETE".equals(rule.getSubmitMode())) {
            // 提交即完成，不创建审批节点。
            request.setOverallStatus(LeaveRequest.APPROVED);
            request.setCurrentNodeOrder(0);
        } else {
            // 需要审批：创建首个节点，申请进入审批中。
            approvalMapper.insertFirstNode(request.getRequestId());
            request.setOverallStatus(LeaveRequest.APPROVING);
            request.setCurrentNodeOrder(1);
        }

        // 6. 更新申请状态并返回结果。
        requestMapper.updateSubmission(request);
        return request;
    }
}

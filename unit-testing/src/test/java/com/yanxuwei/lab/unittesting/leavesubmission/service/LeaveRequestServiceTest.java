package com.yanxuwei.lab.unittesting.leavesubmission.service;

import com.yanxuwei.lab.unittesting.leavesubmission.domain.ApprovalRule;
import com.yanxuwei.lab.unittesting.leavesubmission.domain.LeaveRequest;
import com.yanxuwei.lab.unittesting.leavesubmission.domain.LeaveRequestDto;
import com.yanxuwei.lab.unittesting.leavesubmission.mapper.ApprovalMapper;
import com.yanxuwei.lab.unittesting.leavesubmission.mapper.LeaveRequestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 请假提交业务测试；Mapper 均为模拟对象，不连接数据库。 */
@ExtendWith(MockitoExtension.class)
class LeaveRequestServiceTest {
    @Mock
    private LeaveRequestMapper requestMapper;

    @Mock
    private ApprovalMapper approvalMapper;

    @InjectMocks
    private LeaveRequestService leaveRequestService;

    @Test
    @DisplayName("草稿提交后需要审批：创建首节点并更新为审批中")
    void submitLeaveRequest_whenApprovalRequired_shouldCreateNodeAndSetApproving() {
        // 准备：提交已有草稿，只需要提供申请 ID。
        LeaveRequestDto form = new LeaveRequestDto();
        form.setRequestId(1L);

        // 模拟数据库里查到一条信息完整、状态为待提交的申请。
        LeaveRequest draft = new LeaveRequest();
        draft.setRequestId(1L);
        draft.setApplicantId("user-1");
        draft.setLeaveType("事假");
        draft.setStartTime(LocalDateTime.of(2026, 11, 18, 8, 30));
        draft.setEndTime(LocalDateTime.of(2026, 11, 18, 17, 0));
        draft.setDuration(new BigDecimal("1.0"));
        draft.setReason("家里有事");
        draft.setApplicantMobile("13800000000");
        draft.setDestination("本市");
        draft.setOverallStatus(LeaveRequest.DRAFT);
        when(requestMapper.getById(1L)).thenReturn(draft);
        when(requestMapper.countConflicts(draft)).thenReturn(0);

        // 模拟匹配到一条需要审批的规则。
        ApprovalRule rule = new ApprovalRule();
        rule.setRuleId(7L);
        rule.setSubmitMode("APPROVAL");
        when(approvalMapper.findRule("事假", new BigDecimal("1.0"))).thenReturn(rule);

        // 执行：调用 service.submitLeaveRequest(form)。
        LeaveRequest leaveRequest = leaveRequestService.submitLeaveRequest(form);

        // 断言：检查申请状态、当前节点，以及审批节点和更新操作。
        assertEquals("事假", leaveRequest.getLeaveType());
        assertEquals("user-1", leaveRequest.getApplicantId());
        assertEquals(Integer.valueOf(LeaveRequest.APPROVING), leaveRequest.getOverallStatus());
        assertEquals(Integer.valueOf(1), leaveRequest.getCurrentNodeOrder());
        assertEquals(Long.valueOf(7L), leaveRequest.getMatchedRuleId());

        // 确认创建了该申请的首个审批节点，并把正确状态交给 Mapper 更新。
        verify(approvalMapper).insertFirstNode(1L);
        verify(requestMapper).updateSubmission(argThat(request -> request != null
                && Long.valueOf(1L).equals(request.getRequestId())
                && Integer.valueOf(LeaveRequest.APPROVING).equals(request.getOverallStatus())
                && Integer.valueOf(1).equals(request.getCurrentNodeOrder())
                && Long.valueOf(7L).equals(request.getMatchedRuleId())));

        // 这是提交已有草稿，不应新增申请。
        verify(requestMapper, never()).insert(any());
    }

    @Test
    @DisplayName("规则为提交即完成：不创建审批节点并更新为已完成")
    void submitLeaveRequest_whenAutoComplete_shouldSetApprovedWithoutNode() {
        // 准备：提交已有草稿，只需要提供申请 ID。
        LeaveRequestDto form = new LeaveRequestDto();
        form.setRequestId(1L);

        // 模拟数据库里查到一条信息完整、状态为待提交的申请。
        LeaveRequest draft = new LeaveRequest();
        draft.setRequestId(1L);
        draft.setApplicantId("user-1");
        draft.setLeaveType("事假");
        draft.setStartTime(LocalDateTime.of(2026, 11, 18, 8, 30));
        draft.setEndTime(LocalDateTime.of(2026, 11, 18, 17, 0));
        draft.setDuration(new BigDecimal("1.0"));
        draft.setReason("家里有事");
        draft.setApplicantMobile("13800000000");
        draft.setDestination("本市");
        draft.setOverallStatus(LeaveRequest.DRAFT);
        when(requestMapper.getById(1L)).thenReturn(draft);
        when(requestMapper.countConflicts(draft)).thenReturn(0);

        // 模拟匹配到一条提交即完成的规则。
        ApprovalRule rule = new ApprovalRule();
        rule.setRuleId(1L);
        rule.setSubmitMode("AUTO_COMPLETE");
        when(approvalMapper.findRule("事假", new BigDecimal("1.0"))).thenReturn(rule);

        // 执行：调用 service.submitLeaveRequest(form)。
        LeaveRequest leaveRequest = leaveRequestService.submitLeaveRequest(form);

        // 断言：检查申请状态、当前节点，以及审批节点和更新操作。
        assertEquals("事假", leaveRequest.getLeaveType());
        assertEquals("user-1", leaveRequest.getApplicantId());
        assertEquals(Integer.valueOf(LeaveRequest.APPROVED), leaveRequest.getOverallStatus());
        assertEquals(Integer.valueOf(0), leaveRequest.getCurrentNodeOrder());
        assertEquals(Long.valueOf(1L), leaveRequest.getMatchedRuleId());

        // 提交即完成：不创建审批节点，但要更新已有申请。
        verify(approvalMapper, never()).insertFirstNode(any());
        verify(requestMapper).updateSubmission(draft);

        // 这是提交已有草稿，不应新增申请。
        verify(requestMapper, never()).insert(any());
    }

    @ParameterizedTest(name = "{displayName}：原状态 {0}")
    @ValueSource(ints = {LeaveRequest.REJECTED, LeaveRequest.WITHDRAWN})
    @DisplayName("已驳回或已撤销的申请可以重新提交")
    void submitLeaveRequest_whenRejectedOrWithdrawn_shouldAllowResubmission(int originalStatus) {
        // 准备：重新提交已有申请，只需要提供申请 ID。
        LeaveRequestDto form = new LeaveRequestDto();
        form.setRequestId(1L);

        // 模拟查到一条已驳回或已撤销、信息完整的申请。
        LeaveRequest draft = new LeaveRequest();
        draft.setRequestId(1L);
        draft.setApplicantId("user-1");
        draft.setLeaveType("事假");
        draft.setStartTime(LocalDateTime.of(2026, 11, 18, 8, 30));
        draft.setEndTime(LocalDateTime.of(2026, 11, 18, 17, 0));
        draft.setDuration(new BigDecimal("1.0"));
        draft.setReason("家里有事");
        draft.setApplicantMobile("13800000000");
        draft.setDestination("本市");
        draft.setOverallStatus(originalStatus);
        when(requestMapper.getById(1L)).thenReturn(draft);
        when(requestMapper.countConflicts(draft)).thenReturn(0);

        // 模拟匹配到一条需要审批的规则。
        ApprovalRule rule = new ApprovalRule();
        rule.setRuleId(7L);
        rule.setSubmitMode("APPROVAL");
        when(approvalMapper.findRule("事假", new BigDecimal("1.0"))).thenReturn(rule);

        // 执行：调用 service.submitLeaveRequest(form)。
        LeaveRequest leaveRequest = leaveRequestService.submitLeaveRequest(form);

        // 断言：检查申请状态、当前节点，以及审批节点和更新操作。
        assertEquals("事假", leaveRequest.getLeaveType());
        assertEquals("user-1", leaveRequest.getApplicantId());
        assertEquals(Integer.valueOf(LeaveRequest.APPROVING), leaveRequest.getOverallStatus());
        assertEquals(Integer.valueOf(1), leaveRequest.getCurrentNodeOrder());
        assertEquals(Long.valueOf(7L), leaveRequest.getMatchedRuleId());

        // 确认创建了该申请的首个审批节点，并把正确状态交给 Mapper 更新。
        verify(approvalMapper).insertFirstNode(1L);
        verify(requestMapper).updateSubmission(argThat(request -> request != null
                && Long.valueOf(1L).equals(request.getRequestId())
                && Integer.valueOf(LeaveRequest.APPROVING).equals(request.getOverallStatus())
                && Integer.valueOf(1).equals(request.getCurrentNodeOrder())
                && Long.valueOf(7L).equals(request.getMatchedRuleId())));

        // 这是提交已有草稿，不应新增申请。
        verify(requestMapper, never()).insert(any());
    }

    @Test
    @DisplayName("请假表单为空时，抛出参数异常")
    void submitLeaveRequest_whenFormIsNull_shouldThrowException() {
        LeaveRequestDto form = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> leaveRequestService.submitLeaveRequest(form));

        assertEquals("请假申请不能为空", exception.getMessage());

        verifyNoInteractions(requestMapper, approvalMapper);
    }

    @Test
    @DisplayName("申请 ID 为空时，保存申请并提交审批")
    void submitLeaveRequest_whenRequestIdIsNull_shouldCreateAndSubmit() {
        // 准备：创建完整表单，不设置 requestId，表示新建申请。
        LeaveRequestDto form = new LeaveRequestDto();
        form.setApplicantId("user-1");
        form.setLeaveType("事假");
        form.setStartTime(LocalDateTime.of(2026, 11, 18, 8, 30));
        form.setEndTime(LocalDateTime.of(2026, 11, 18, 17, 0));
        form.setDuration(new BigDecimal("1.0"));
        form.setReason("家里有事");
        form.setApplicantMobile("13800000000");
        form.setDestination("本市");

        // 模拟保存：给新申请设置数据库生成的 ID。
        doAnswer(invocation -> {
            LeaveRequest request = invocation.getArgument(0);
            request.setRequestId(100L);
            return 1;
        }).when(requestMapper).insert(any(LeaveRequest.class));

        // 模拟匹配到需要审批的规则。
        ApprovalRule rule = new ApprovalRule();
        rule.setRuleId(7L);
        rule.setSubmitMode("APPROVAL");

        when(approvalMapper.findRule("事假", new BigDecimal("1.0")))
                .thenReturn(rule);

        // 执行。
        LeaveRequest result = leaveRequestService.submitLeaveRequest(form);

        // 断言返回的申请使用了新生成的 ID，并进入审批中。
        assertEquals(Long.valueOf(100L), result.getRequestId());
        assertEquals(Integer.valueOf(LeaveRequest.APPROVING), result.getOverallStatus());
        assertEquals(Integer.valueOf(1), result.getCurrentNodeOrder());
        assertEquals(Long.valueOf(7L), result.getMatchedRuleId());

        // 检查新增申请、创建审批节点和更新状态都被调用。
        verify(requestMapper).insert(any(LeaveRequest.class));
        verify(approvalMapper).insertFirstNode(100L);
        verify(requestMapper).updateSubmission(result);

        // 新建申请不需要根据 ID 查询已有草稿。
        verify(requestMapper, never()).getById(any());
    }

    @Test
    @DisplayName("申请不存在时，不应写入数据")
    void submitLeaveRequest_whenRequestDoesNotExist_shouldThrowException() {
        LeaveRequestDto form = formWithId();
        when(requestMapper.getById(1L)).thenReturn(null);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> leaveRequestService.submitLeaveRequest(form));

        assertEquals("请假申请不存在", error.getMessage());
        verifyNoInteractions(approvalMapper);
        verify(requestMapper, never()).insert(any());
        verify(requestMapper, never()).updateSubmission(any());
    }

    @ParameterizedTest(name = "{displayName}：状态 {0}")
    @ValueSource(ints = {LeaveRequest.APPROVING, LeaveRequest.APPROVED})
    @DisplayName("审批中或已完成的申请不能重复提交")
    void submitLeaveRequest_whenStatusCannotBeSubmitted_shouldThrowException(int status) {
        LeaveRequest draft = validDraft();
        draft.setOverallStatus(status);
        when(requestMapper.getById(1L)).thenReturn(draft);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> leaveRequestService.submitLeaveRequest(formWithId()));

        assertEquals("当前请假申请不可重复提交", error.getMessage());
        verifyNoWrites();
        verifyNoInteractions(approvalMapper);
    }

    @ParameterizedTest(name = "{displayName}：{0}")
    @ValueSource(strings = {"applicant", "type", "start", "end", "duration", "reason", "mobile", "destination"})
    @DisplayName("请假必填信息缺失时，不应写入数据")
    void submitLeaveRequest_whenRequiredFieldsAreMissing_shouldThrowException(String field) {
        LeaveRequest draft = validDraft();
        switch (field) {
            case "applicant": draft.setApplicantId(null); break;
            case "type": draft.setLeaveType(" "); break;
            case "start": draft.setStartTime(null); break;
            case "end": draft.setEndTime(null); break;
            case "duration": draft.setDuration(null); break;
            case "reason": draft.setReason(" "); break;
            case "mobile": draft.setApplicantMobile(null); break;
            case "destination": draft.setDestination(" "); break;
            default: throw new IllegalArgumentException(field);
        }
        when(requestMapper.getById(1L)).thenReturn(draft);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> leaveRequestService.submitLeaveRequest(formWithId()));

        assertEquals("请完整填写请假信息", error.getMessage());
        verifyNoWrites();
        verifyNoInteractions(approvalMapper);
    }

    @Test
    @DisplayName("请假开始时间不早于结束时间时，抛出参数异常")
    void submitLeaveRequest_whenTimeRangeIsInvalid_shouldThrowException() {
        LeaveRequest draft = validDraft();
        draft.setStartTime(draft.getEndTime());
        when(requestMapper.getById(1L)).thenReturn(draft);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> leaveRequestService.submitLeaveRequest(formWithId()));

        assertEquals("请假时间或时长不合法", error.getMessage());
        verifyNoWrites();
        verifyNoInteractions(approvalMapper);
    }

    @ParameterizedTest(name = "{displayName}：{0} 天")
    @ValueSource(strings = {"0", "-1", "1.01"})
    @DisplayName("请假时长不为正数或超过一位小数时，抛出参数异常")
    void submitLeaveRequest_whenDurationIsInvalid_shouldThrowException(String duration) {
        LeaveRequest draft = validDraft();
        draft.setDuration(new BigDecimal(duration));
        when(requestMapper.getById(1L)).thenReturn(draft);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> leaveRequestService.submitLeaveRequest(formWithId()));

        assertEquals("请假时间或时长不合法", error.getMessage());
        verifyNoWrites();
        verifyNoInteractions(approvalMapper);
    }

    @Test
    @DisplayName("年休假时长不是整天时，抛出参数异常")
    void submitLeaveRequest_whenAnnualLeaveIsNotWholeDays_shouldThrowException() {
        LeaveRequest draft = validDraft();
        draft.setLeaveType("年休假");
        draft.setDuration(new BigDecimal("0.5"));
        when(requestMapper.getById(1L)).thenReturn(draft);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> leaveRequestService.submitLeaveRequest(formWithId()));

        assertEquals("年休假时长必须为整天", error.getMessage());
        verifyNoWrites();
        verifyNoInteractions(approvalMapper);
    }

    @Test
    @DisplayName("与已有请假时间冲突时，抛出参数异常")
    void submitLeaveRequest_whenTimeConflicts_shouldThrowException() {
        LeaveRequest draft = validDraft();
        when(requestMapper.getById(1L)).thenReturn(draft);
        when(requestMapper.countConflicts(draft)).thenReturn(1);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> leaveRequestService.submitLeaveRequest(formWithId()));

        assertEquals("该请假时间与已有请假申请冲突", error.getMessage());
        verifyNoWrites();
        verifyNoInteractions(approvalMapper);
    }

    @Test
    @DisplayName("没有匹配的审批规则时，抛出参数异常")
    void submitLeaveRequest_whenApprovalRuleIsMissing_shouldThrowException() {
        LeaveRequest draft = validDraft();
        when(requestMapper.getById(1L)).thenReturn(draft);
        when(approvalMapper.findRule("事假", new BigDecimal("1.0"))).thenReturn(null);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> leaveRequestService.submitLeaveRequest(formWithId()));

        assertEquals("未匹配到可用的审批规则", error.getMessage());
        verifyNoWrites();
        verify(approvalMapper, never()).insertFirstNode(any());
    }

    @Test
    @DisplayName("审批规则处理方式无效时，抛出参数异常")
    void submitLeaveRequest_whenSubmitModeIsInvalid_shouldThrowException() {
        LeaveRequest draft = validDraft();
        ApprovalRule rule = new ApprovalRule();
        rule.setRuleId(7L);
        rule.setSubmitMode("UNKNOWN");
        when(requestMapper.getById(1L)).thenReturn(draft);
        when(approvalMapper.findRule("事假", new BigDecimal("1.0"))).thenReturn(rule);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> leaveRequestService.submitLeaveRequest(formWithId()));

        assertEquals("审批规则处理方式无效", error.getMessage());
        verifyNoWrites();
        verify(approvalMapper, never()).insertFirstNode(any());
    }

    @Test
    @DisplayName("新申请匹配到无效规则时，不应新增申请")
    void submitLeaveRequest_whenNewRequestHasInvalidRule_shouldNotInsert() {
        LeaveRequestDto form = new LeaveRequestDto();
        form.setApplicantId("user-1");
        form.setLeaveType("事假");
        form.setStartTime(LocalDateTime.of(2026, 11, 18, 8, 30));
        form.setEndTime(LocalDateTime.of(2026, 11, 18, 17, 0));
        form.setDuration(new BigDecimal("1.0"));
        form.setReason("家里有事");
        form.setApplicantMobile("13800000000");
        form.setDestination("本市");
        ApprovalRule rule = new ApprovalRule();
        rule.setRuleId(7L);
        rule.setSubmitMode("UNKNOWN");
        when(approvalMapper.findRule("事假", new BigDecimal("1.0"))).thenReturn(rule);

        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> leaveRequestService.submitLeaveRequest(form));

        assertEquals("审批规则处理方式无效", error.getMessage());
        verifyNoWrites();
        verify(approvalMapper, never()).insertFirstNode(any());
    }

    /** 每个异常场景都确认没有新增或更新申请。 */
    private void verifyNoWrites() {
        verify(requestMapper, never()).insert(any());
        verify(requestMapper, never()).updateSubmission(any());
    }

    private LeaveRequestDto formWithId() {
        LeaveRequestDto form = new LeaveRequestDto();
        form.setRequestId(1L);
        return form;
    }

    private LeaveRequest validDraft() {
        LeaveRequest draft = new LeaveRequest();
        draft.setRequestId(1L);
        draft.setApplicantId("user-1");
        draft.setLeaveType("事假");
        draft.setStartTime(LocalDateTime.of(2026, 11, 18, 8, 30));
        draft.setEndTime(LocalDateTime.of(2026, 11, 18, 17, 0));
        draft.setDuration(new BigDecimal("1.0"));
        draft.setReason("家里有事");
        draft.setApplicantMobile("13800000000");
        draft.setDestination("本市");
        draft.setOverallStatus(LeaveRequest.DRAFT);
        return draft;
    }
}

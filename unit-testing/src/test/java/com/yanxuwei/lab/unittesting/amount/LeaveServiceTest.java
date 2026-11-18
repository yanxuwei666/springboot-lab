package com.yanxuwei.lab.unittesting.amount;

import com.yanxuwei.lab.unittesting.amount.domain.LeaveApply;
import com.yanxuwei.lab.unittesting.amount.service.LeaveService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 请假申请业务层单元测试类
 *
 * @author yxw
 * @version 1.0
 * @since 2026/11/18
 */
public class LeaveServiceTest {

    @Test
    @DisplayName("请假天数为 3 天时，返回申请成功")
    void submitLeaveApply_shouldReturnSuccessWhenDaysIsValid() {
        // 准备：创建服务和请假申请，设置请假天数为 3
        LeaveApply leaveApply = new LeaveApply(3);
        LeaveService leaveService = new LeaveService();

        // 执行：调用 submitLeaveApply
        String result = leaveService.submitLeaveApply(leaveApply);

        // 断言：检查返回结果是否为「申请成功」
        assertEquals("申请成功", result);
    }

    @Test
    @DisplayName("请假天数为 1 天时，返回申请成功")
    void submitLeaveApply_shouldReturnSuccessWhenDaysIsOne() {
        // 准备：创建服务和请假申请，设置请假天数为 1
        LeaveApply leaveApply = new LeaveApply(1);
        LeaveService leaveService = new LeaveService();

        // 执行：调用 submitLeaveApply
        String result = leaveService.submitLeaveApply(leaveApply);

        // 断言：检查返回结果是否为「申请成功」
        assertEquals("申请成功", result);
    }

    @Test
    @DisplayName("请假天数为 5 天时，返回申请成功")
    void submitLeaveApply_shouldReturnSuccessWhenDaysIsFive() {
        // 准备：创建服务和请假申请，设置请假天数为 5
        LeaveApply leaveApply = new LeaveApply(5);
        LeaveService leaveService = new LeaveService();

        // 执行：调用 submitLeaveApply
        String result = leaveService.submitLeaveApply(leaveApply);

        // 断言：检查返回结果是否为「申请成功」
        assertEquals("申请成功", result);
    }

    @Test
    @DisplayName("请假天数为 0 天时，返回错误提示")
    void submitLeaveApply_shouldReturnErrorWhenDaysIsLessThanOne() {
        // 准备：创建服务和请假申请，设置请假天数为 0
        LeaveApply leaveApply = new LeaveApply(0);
        LeaveService leaveService = new LeaveService();

        // 执行：调用 submitLeaveApply
        String result = leaveService.submitLeaveApply(leaveApply);

        // 断言：检查返回结果是否为「请假天数必须在 1 到 5 天之间」
        assertEquals("请假天数必须在 1 到 5 天之间", result);
    }

    @Test
    @DisplayName("请假天数为 6 天时，返回错误提示")
    void submitLeaveApply_shouldReturnErrorWhenDaysIsGreaterThanFive() {
        // 准备：创建服务和请假申请，设置请假天数为 6
        LeaveApply leaveApply = new LeaveApply(6);
        LeaveService leaveService = new LeaveService();

        // 执行：调用 submitLeaveApply
        String result = leaveService.submitLeaveApply(leaveApply);

        // 断言：检查返回结果是否为「请假天数必须在 1 到 5 天之间」
        assertEquals("请假天数必须在 1 到 5 天之间", result);
    }

    @Test
    @DisplayName("请假天数为空时，返回错误提示")
    void submitLeaveApply_shouldReturnErrorWhenDaysIsNull() {
        // 准备：创建服务和请假申请，设置请假天数为空
        LeaveApply leaveApply = new LeaveApply();
        LeaveService leaveService = new LeaveService();

        // 执行：调用 submitLeaveApply
        String result = leaveService.submitLeaveApply(leaveApply);

        // 断言：检查返回结果是否为「请假天数必须在 1 到 5 天之间」
        assertEquals("请假天数必须在 1 到 5 天之间", result);
    }

    @Test
    @DisplayName("请假申请对象为空时，返回错误提示")
    void submitLeaveApply_shouldReturnErrorWhenApplicationIsNull() {
        // 准备：创建服务和请假申请，设置申请对象为空
        LeaveApply leaveApply = null;
        LeaveService leaveService = new LeaveService();

        // 执行：调用 submitLeaveApply
        String result = leaveService.submitLeaveApply(leaveApply);

        // 断言：检查返回结果是否为「请假天数必须在 1 到 5 天之间」
        assertEquals("请假天数必须在 1 到 5 天之间", result);
    }
}

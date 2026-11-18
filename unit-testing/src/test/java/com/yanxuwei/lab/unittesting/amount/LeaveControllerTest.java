package com.yanxuwei.lab.unittesting.amount;

import com.yanxuwei.lab.unittesting.amount.controller.LeaveController;
import com.yanxuwei.lab.unittesting.amount.domain.LeaveApply;
import com.yanxuwei.lab.unittesting.amount.service.LeaveService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LeaveController.class)
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveService leaveService;

    @Test
    @DisplayName("提交 3 天请假申请时，返回申请成功")
    void submitLeaveApply_shouldReturnSuccessWhenServiceSucceeds() throws Exception {
        // 准备：模拟 Service 返回的结果。
        when(leaveService.submitLeaveApply(any(LeaveApply.class))).thenReturn("申请成功");

        // 执行并断言：发送 JSON 请求，检查 HTTP 状态和响应内容。
        mockMvc.perform(post("/api/v1/leave/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"days\": 3}"))
                .andExpect(status().isOk())
                .andExpect(content().string("申请成功"));

        // 检查 Controller 传给 Service 的请假天数。
        verify(leaveService).submitLeaveApply(argThat(apply -> apply != null && Integer.valueOf(3).equals(apply.getDays())));
    }

    @Test
    @DisplayName("提交 0 天请假申请时，返回请假天数必须在 1 到 5 天之间")
    void submitLeaveApply_shouldReturnErrorMessageWhenServiceRejectsApplication() throws Exception {
        // 准备：模拟 Service 返回的结果。
        when(leaveService.submitLeaveApply(any(LeaveApply.class))).thenReturn("请假天数必须在 1 到 5 天之间");

        // 执行并断言：发送 JSON 请求，检查 HTTP 状态和响应内容。
        mockMvc.perform(post("/api/v1/leave/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"days\": 0}"))
                .andExpect(status().isOk())
                .andExpect(content().string("请假天数必须在 1 到 5 天之间"));

        // 检查 Controller 传给 Service 的请假天数。
        verify(leaveService).submitLeaveApply(argThat(apply -> apply != null && Integer.valueOf(0).equals(apply.getDays())));
    }

    @Test
    @DisplayName("请求体缺失时，返回 HTTP 400")
    void submitLeaveApply_shouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/leave/apply")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // 请求未通过解析，不应调用 Service。
        verifyNoInteractions(leaveService);
    }

    @Test
    @DisplayName("请求体不是合法 JSON 时，返回 HTTP 400")
    void submitLeaveApply_shouldReturnBadRequestWhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/api/v1/leave/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"days\": }"))
                .andExpect(status().isBadRequest());

        // 请求未通过解析，不应调用 Service。
        verifyNoInteractions(leaveService);
    }
}

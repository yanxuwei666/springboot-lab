package com.yanxuwei.lab.unittesting.leavesubmission.controller;

import com.yanxuwei.lab.unittesting.leavesubmission.domain.LeaveRequest;
import com.yanxuwei.lab.unittesting.leavesubmission.domain.LeaveRequestDto;
import com.yanxuwei.lab.unittesting.leavesubmission.service.LeaveRequestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 请假提交接口测试；Service 为模拟对象，不连接数据库。 */
@WebMvcTest(LeaveSubmissionController.class)
class LeaveSubmissionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaveRequestService service;

    @Test
    @DisplayName("提交成功时，返回统一响应并传递请假表单")
    void submit_whenServiceSucceeds_shouldReturnSuccess() throws Exception {
        LeaveRequest result = new LeaveRequest();
        result.setRequestId(100L);
        result.setOverallStatus(LeaveRequest.APPROVING);
        when(service.submitLeaveRequest(any(LeaveRequestDto.class))).thenReturn(result);

        mockMvc.perform(post("/api/leave/requests/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"applicantId\":\"user-1\",\"leaveType\":\"事假\","
                                + "\"startTime\":\"2026-11-18 08:30:00\",\"endTime\":\"2026-11-18 17:00:00\","
                                + "\"duration\":1.0,\"reason\":\"家里有事\",\"applicantMobile\":\"13800000000\",\"destination\":\"本市\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("成功"))
                .andExpect(jsonPath("$.data.requestId").value(100))
                .andExpect(jsonPath("$.data.overallStatus").value(LeaveRequest.APPROVING));

        verify(service).submitLeaveRequest(argThat(form -> form != null
                && form.getRequestId() == null
                && "user-1".equals(form.getApplicantId())
                && "事假".equals(form.getLeaveType())
                && form.getDuration() != null
                && form.getDuration().compareTo(new BigDecimal("1.0")) == 0
                && form.getStartTime() != null && form.getStartTime().getHour() == 8));
    }

    @Test
    @DisplayName("业务校验失败时，返回统一错误响应")
    void submit_whenServiceThrowsException_shouldReturnError() throws Exception {
        when(service.submitLeaveRequest(any(LeaveRequestDto.class)))
                .thenThrow(new IllegalArgumentException("请完整填写请假信息"));

        mockMvc.perform(post("/api/leave/requests/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"applicantId\":\"user-1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("请完整填写请假信息"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(service).submitLeaveRequest(argThat(form -> form != null && "user-1".equals(form.getApplicantId())));
    }

    @Test
    @DisplayName("请求体不是合法 JSON 时，返回 HTTP 400")
    void submit_whenJsonIsMalformed_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/leave/requests/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"duration\": }"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    @DisplayName("请求体缺失时，返回 HTTP 400")
    void submit_whenBodyIsMissing_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/leave/requests/submit").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }
}

package com.yanxuwei.lab.unittesting.leavesubmission.controller;

import com.yanxuwei.lab.unittesting.common.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = LeaveSubmissionController.class)
public class LeaveExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleInvalidRequest(IllegalArgumentException exception) {
        return Result.error(400, exception.getMessage());
    }
}

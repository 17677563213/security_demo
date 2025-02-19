package com.windsurfing.security.exception;

import com.windsurfing.security.model.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ApiResponse<String> handleException(Exception e) {
        return ApiResponse.error(500, e.getMessage());
    }
}

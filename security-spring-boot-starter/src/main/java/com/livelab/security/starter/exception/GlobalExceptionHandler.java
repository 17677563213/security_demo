package com.livelab.security.starter.exception;

import com.livelab.security.starter.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SecurityException.class)
    public ApiResponse<Void> handleSecurityException(SecurityException e) {
        log.error("Security error: ", e);
        return ApiResponse.error(500, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("System error: ", e);
        return ApiResponse.error(500, "Internal server error");
    }
}

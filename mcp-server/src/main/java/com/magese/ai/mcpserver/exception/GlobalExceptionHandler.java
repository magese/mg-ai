package com.magese.ai.mcpserver.exception;

import com.magese.ai.mcpserver.domain.McpErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理
 *
 * @author Magese
 * @since 2025/8/27 10:01
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<McpErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        McpErrorResponse error = new McpErrorResponse(
                "Invalid_Input",
                "Invalid input parameter: " + ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

}

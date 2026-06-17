package com.tracking.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ApiError {
    private String code;
    private String message;
    private int status;
    private String path;
    private LocalDateTime timestamp;
}

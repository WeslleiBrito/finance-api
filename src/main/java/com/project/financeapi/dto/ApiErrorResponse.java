package com.project.financeapi.dto;

import java.time.LocalDateTime;
import java.util.Map;


public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        String path,
        Map<String, String> errors
) {}

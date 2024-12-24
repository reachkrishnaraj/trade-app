package com.kraj.tradeapp.core.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ApiError {

    private HttpStatus status;
    private String message;
    private List<String> errors;
    private LocalDateTime timestamp;
}

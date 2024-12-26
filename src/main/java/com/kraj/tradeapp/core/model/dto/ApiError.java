package com.kraj.tradeapp.core.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.*;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ApiError {

    private HttpStatus status;
    private String message;
    private List<String> errors;
    private LocalDateTime timestamp;
}

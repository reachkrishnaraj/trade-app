package com.kraj.tradeapp.core.model.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeSignalRequest {

    @NotBlank(message = "Symbol is required")
    @Size(max = 50)
    private String symbol;

    @NotNull(message = "Signal type is required")
    @Pattern(regexp = "^(BUY|SELL|HOLD)$", message = "Signal type must be BUY, SELL, or HOLD")
    private String signalType;

    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal confidence;

    private String reason;

    @NotBlank(message = "Source is required")
    @Size(max = 50)
    private String source;
}

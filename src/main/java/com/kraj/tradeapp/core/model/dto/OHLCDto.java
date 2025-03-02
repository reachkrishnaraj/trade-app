package com.kraj.tradeapp.core.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OHLCDto {

    @JsonProperty("d")
    private long epochMillis;

    @JsonProperty("o")
    private BigDecimal open;

    @JsonProperty("h")
    private BigDecimal high;

    @JsonProperty("l")
    private BigDecimal low;

    @JsonProperty("c")
    private BigDecimal close;
}

package com.kraj.tradeapp.core.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OHLCMarketDto {

    @JsonProperty("sym")
    private String symbol;

    private List<OHLCDto> ohlc;
}

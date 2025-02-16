package com.kraj.tradeapp.core.model.pickmytrade;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeAccount {

    private String token;

    @JsonProperty("account_id")
    private String accountId;

    @JsonProperty("risk_percentage")
    private int riskPercentage;

    @JsonProperty("account_type")
    private int quantity_multiplier;
}

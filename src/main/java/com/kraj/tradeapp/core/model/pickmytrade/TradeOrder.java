package com.kraj.tradeapp.core.model.pickmytrade;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TradeOrder {

    private String symbol;

    private String date;

    private String data;

    private int quantity;

    @JsonProperty("risk_percentage")
    private int riskPercentage;

    private String price;

    @JsonProperty("gtd_in_second")
    private int gtdInSecond;

    private int tp;

    @JsonProperty("percentage_tp")
    private int percentageTp;

    @JsonProperty("dollar_tp")
    private int dollarTp;

    private int sl;

    @JsonProperty("percentage_sl")
    private int percentageSl;

    @JsonProperty("dollar_sl")
    private int dollarSl;

    private int trail;

    @JsonProperty("trail_stop")
    private int trailStop;

    @JsonProperty("trail_trigger")
    private int trailTrigger;

    @JsonProperty("trail_freq")
    private int trailFreq;

    @JsonProperty("update_tp")
    private boolean updateTp;

    @JsonProperty("update_sl")
    private boolean updateSl;

    @JsonProperty("breakeven")
    private int breakeven;

    private String token;

    private boolean pyramid;

    @JsonProperty("reverse_order_close")
    private boolean reverseOrderClose;

    @JsonProperty("order_type")
    private String orderType;

    @JsonProperty("multiple_accounts")
    private List<TradeAccount> multipleAccounts;

    @JsonProperty("duplicate_position_allow")
    private boolean duplicatePositionAllow;
}

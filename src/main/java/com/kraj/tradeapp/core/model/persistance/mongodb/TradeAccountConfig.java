package com.kraj.tradeapp.core.model.persistance.mongodb;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
@Document(collection = "trade_account_config")
@NoArgsConstructor
@AllArgsConstructor
public class TradeAccountConfig {

    @Id
    private String id;

    @Indexed
    private String accGroupName;

    @Indexed
    private String accTradeGroupName;

    @Indexed
    private String ownerName;

    private String accId;

    private String accName;

    private String accType;

    private String tradePlatform;

    @Indexed
    private String pickMyTradeToken;

    @Indexed
    private String symbol;

    private boolean useTakeProfit;

    private int takeProfitTicks;

    private boolean useStopLoss;

    private int stopLossTicks;

    private boolean useBreakEven;

    private int breakEvenTicks;

    private boolean useTrailingStop;

    private int trailingStopTicks;

    private int quantity;

    private BigDecimal perTickDollarValue;

    private boolean tradeEnabled;

    private String automationPlatform;
}

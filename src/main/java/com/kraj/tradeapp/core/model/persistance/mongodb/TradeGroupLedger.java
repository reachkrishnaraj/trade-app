package com.kraj.tradeapp.core.model.persistance.mongodb;

import com.kraj.tradeapp.core.model.TradeDirection;
import com.kraj.tradeapp.core.model.TradeStatus;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
@Document(collection = "trade_group_ledger")
@NoArgsConstructor
@AllArgsConstructor
public class TradeGroupLedger {

    @Id
    private String id;

    @Indexed
    private String tradeAccGroupName;

    private String tradeAccGroupOwnerName;

    @Indexed
    private String symbol;

    @Indexed
    private String parentSymbol;

    private String tradePlatform;

    private BigDecimal entryPrice;

    private BigDecimal exitPrice;

    private BigDecimal takeProfitPrice;

    private BigDecimal stopLossPrice;

    @Indexed(expireAfterSeconds = 60 * 24 * 60 * 60) // 60 days
    private ZonedDateTime created;

    private ZonedDateTime lastUpdated;

    private ZonedDateTime entryTime;

    private ZonedDateTime exitTime;

    private BigDecimal profitLoss;

    private BigDecimal profitLossPercentage;

    private TradeDirection tradeDirection;

    private TradeStatus tradeStatus;
}

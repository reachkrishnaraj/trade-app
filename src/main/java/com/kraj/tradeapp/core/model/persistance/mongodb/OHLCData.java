package com.kraj.tradeapp.core.model.persistance.mongodb;

import com.kraj.tradeapp.core.model.TimeFrame;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ohlc_data")
@CompoundIndex(name = "symbol_timestamp_idx", def = "{'symbol': 1, 'timestamp': 1}")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class OHLCData {

    @Id
    private long id; //epochmillis

    private long nyDateTimeId; //YYYYMMDDHHMM

    private String symbol; // "NQ", "ES"

    @Indexed(name = "timestamp_idx", direction = IndexDirection.ASCENDING)
    private Instant timestamp; // Stored in UTC, convert to NY when needed

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal close;

    private TimeFrame timeframe; // "1m", "5m", "15m", "1h", "1d"
}

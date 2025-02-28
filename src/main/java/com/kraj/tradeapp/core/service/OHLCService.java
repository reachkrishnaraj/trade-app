package com.kraj.tradeapp.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraj.tradeapp.core.model.CommonUtil;
import com.kraj.tradeapp.core.model.TimeFrame;
import com.kraj.tradeapp.core.model.dto.OHLCMarketDto;
import com.kraj.tradeapp.core.model.persistance.mongodb.OHLCData;
import com.kraj.tradeapp.core.repository.mongodb.OHLCRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OHLCService {

    private final OHLCRepository ohlcRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void handleOHLCDataPost(String payload) throws JsonProcessingException {
        // Parse the payload and save the OHLC data
        OHLCMarketDto ohlcMarketDto = objectMapper.readValue(payload, OHLCMarketDto.class);
        List<OHLCData> ohlcDataList = new ArrayList<>();
        ohlcMarketDto
            .getOhlc()
            .forEach(ohlcDto -> {
                OHLCData ohlcData = OHLCData.builder()
                    .id(ohlcDto.getEpochMillis())
                    .nyDateTimeId(CommonUtil.getTimeIndexNY(ohlcDto.getEpochMillis()))
                    .symbol(ohlcMarketDto.getSymbol())
                    .timestamp(Instant.ofEpochMilli(ohlcDto.getEpochMillis()))
                    .open(ohlcDto.getOpen())
                    .high(ohlcDto.getHigh())
                    .low(ohlcDto.getLow())
                    .close(ohlcDto.getClose())
                    .timeframe(TimeFrame.M1)
                    .build();
                ohlcDataList.add(ohlcData);
            });
        ohlcRepository.saveAll(ohlcDataList);
    }

    public void saveOHLCData(OHLCData ohlcData) {
        ohlcRepository.save(ohlcData);
    }

    public Optional<OHLCData> aggregateOHLC(String symbol, ZonedDateTime start, ZonedDateTime end) {
        List<OHLCData> ohlcDataList = ohlcRepository.findBySymbolAndTimestampBetween(symbol, start, end);

        if (ohlcDataList.isEmpty()) {
            return Optional.empty();
        }

        BigDecimal open = ohlcDataList.get(0).getOpen();
        BigDecimal high = BigDecimal.valueOf(ohlcDataList.stream().mapToDouble(p -> p.getHigh().doubleValue()).max().orElse(0));
        BigDecimal low = BigDecimal.valueOf(ohlcDataList.stream().mapToDouble(p -> p.getLow().doubleValue()).min().orElse(0));
        BigDecimal close = ohlcDataList.get(ohlcDataList.size() - 1).getClose();

        Instant openDateTime = ohlcDataList.get(0).getTimestamp();
        OHLCData aggregatedOHLC = new OHLCData();
        aggregatedOHLC.setSymbol(symbol);
        aggregatedOHLC.setTimestamp(openDateTime); // Use the open time as the timestamp
        aggregatedOHLC.setOpen(open);
        aggregatedOHLC.setHigh(high);
        aggregatedOHLC.setLow(low);
        aggregatedOHLC.setClose(close);

        return Optional.of(aggregatedOHLC);
    }
}

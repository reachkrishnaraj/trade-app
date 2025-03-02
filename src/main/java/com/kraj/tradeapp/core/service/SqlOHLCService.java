package com.kraj.tradeapp.core.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraj.tradeapp.core.model.CommonUtil;
import com.kraj.tradeapp.core.model.dto.OHLCDto;
import com.kraj.tradeapp.core.model.dto.OHLCMarketDto;
import com.kraj.tradeapp.core.model.persistance.AggregatedSqlOHLC;
import com.kraj.tradeapp.core.model.persistance.AggregatedSqlOHLCProjection;
import com.kraj.tradeapp.core.model.persistance.SqlOHLCData;
import com.kraj.tradeapp.core.repository.AggregatedSqlOHLCRepository;
import com.kraj.tradeapp.core.repository.SqlOhlcDataRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SqlOHLCService {

    ObjectMapper objectMapper = new ObjectMapper();

    private final AggregatedSqlOHLCRepository aggregatedOHLCRepository;
    private final SqlOhlcDataRepository ohlcRepository;

    @Transactional
    public void aggregateOHLC(String timeframe, long intervalMillis, int lookbackMultiplier) {
        long lookbackMillis = lookbackMultiplier * intervalMillis;
        aggregatedOHLCRepository.aggregateOHLC(intervalMillis, intervalMillis, timeframe, lookbackMillis);
    }

    //    protected OHLCMarketDto getCurrDateModified(OHLCMarketDto ohlcMarketDto) {
    //        List<OHLCDto> ohlc = ohlcMarketDto.getOhlc();
    //        List<OHLCDto> ohlcCopy = new ArrayList<>();
    //
    //        for(int i=0; i < 48; i++) {
    //            Instant now = Instant.now().minus(i, ChronoUnit.HOURS);
    //
    //            System.out.println("🔍 Current Time: " + now.toString());
    //
    //            for(int j=0; j < 60; j++) {
    //                Instant time = now.plus(j, ChronoUnit.MINUTES);
    //                long epochMillis = time.toEpochMilli();
    //                OHLCDto dto = OHLCDto.builder()
    //                    .epochMillis(epochMillis).open(ohlc.get(j).getOpen()).high(ohlc.get(j).getHigh()).low(ohlc.get(j).getLow()).close(ohlc.get(j).getClose()).build();
    //                ohlcCopy.add(dto);
    //            }
    //        }
    //
    //        //group by milliseconds
    //        Map<Long, List<OHLCDto>> groupedByEpochMillis = ohlcCopy.stream().collect(Collectors.groupingBy(OHLCDto::getEpochMillis));
    //
    //        ohlcMarketDto.setOhlc(ohlcCopy);
    //        return ohlcMarketDto;
    //    }

    //    public void findDuplicateOHLCRecords(List<SqlOHLCData> ohlcDataList) {
    //        // ✅ Group by (symbol, timestamp) and count occurrences
    //        Map<String, Long> duplicates = ohlcDataList.stream()
    //            .collect(Collectors.groupingBy(
    //                data -> data.getSymbol() + "_" + data.getTimestamp(),
    //                Collectors.counting()
    //            ));
    //
    //        // ✅ Filter duplicates (count > 1)
    //        List<String> duplicateKeys = duplicates.entrySet().stream()
    //            .filter(entry -> entry.getValue() > 1)
    //            .map(Map.Entry::getKey)
    //            .collect(Collectors.toList());
    //
    //        // ✅ Print duplicate records
    //        if (!duplicateKeys.isEmpty()) {
    //            System.out.println("❌ Found Duplicate OHLC Entries:");
    //            duplicateKeys.forEach(System.out::println);
    //        } else {
    //            System.out.println("✅ No duplicates found!");
    //        }
    //    }

    public void handleOHLCDataPost(String payload) throws JsonProcessingException {
        // Parse the payload and save the OHLC data
        OHLCMarketDto ohlcMarketDto = objectMapper.readValue(payload, OHLCMarketDto.class);

        List<SqlOHLCData> ohlcDataList = new ArrayList<>();

        for (OHLCDto dto : ohlcMarketDto.getOhlc()) {
            SqlOHLCData ohlcData = SqlOHLCData.builder()
                .nyDateTimeId(CommonUtil.getTimeIndexNY(dto.getEpochMillis()))
                .symbol(ohlcMarketDto.getSymbol())
                .timestamp(Instant.ofEpochMilli(dto.getEpochMillis()))
                .open(dto.getOpen())
                .high(dto.getHigh())
                .low(dto.getLow())
                .close(dto.getClose())
                .build();
            ohlcDataList.add(ohlcData);
        }

        int batchSize = 100;
        for (int i = 0; i < ohlcDataList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, ohlcDataList.size());

            List<SqlOHLCData> batch = ohlcDataList.subList(i, end);

            try {
                ohlcRepository.saveAll(batch);
            } catch (Exception e) {
                System.err.println("Failed batch at index " + i);
                e.printStackTrace();
            }
        }
    }

    @Transactional
    public void aggregateAndSaveOHLC(String timeframe, long intervalMillis, int lookbackMultiplier) {
        long lookbackMillis = intervalMillis + (lookbackMultiplier * intervalMillis);

        // Fetch aggregated data
        List<AggregatedSqlOHLCProjection> rawData = aggregatedOHLCRepository.fetchAggregatedOHLC_futuresV7(
            intervalMillis,
            timeframe,
            lookbackMillis
        );

        // Convert to AggregatedOHLC entities
        List<AggregatedSqlOHLC> aggregatedOhlcs = rawData
            .stream()
            .map(row -> {
                AggregatedSqlOHLC ohlc = new AggregatedSqlOHLC();
                //Timestamp timestamp = (Timestamp) row[0];
                //            ohlc.setId(((Number) row[0]).longValue());
                ohlc.setSymbol(row.getSymbol());
                ohlc.setTimeframe(row.getTimeframe());
                ohlc.setTimestamp(row.getTimestamp());
                ohlc.setNyDateTimeId(CommonUtil.getTimeIndexNY(row.getTimestamp().toEpochMilli()));
                ohlc.setOpen(row.getOpen());
                ohlc.setHigh(row.getHigh());
                ohlc.setLow(row.getLow());
                ohlc.setClose(row.getClose());
                return ohlc;
            })
            .collect(Collectors.toList());

        // Save all records to the database
        aggregatedOHLCRepository.saveAll(aggregatedOhlcs);
    }
}

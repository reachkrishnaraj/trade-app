package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.ParentSymbol;
import com.kraj.tradeapp.core.model.TradeDirection;
import com.kraj.tradeapp.core.model.TradeStatus;
import com.kraj.tradeapp.core.model.persistance.mongodb.TradeAccountConfig;
import com.kraj.tradeapp.core.model.persistance.mongodb.TradeGroupLedger;
import com.kraj.tradeapp.core.repository.mongodb.TradeGroupLedgerRepository;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Null;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TradeGroupLedgerService {

    private final TradeGroupLedgerRepository tradeGroupLedgerRepository;

    public List<TradeGroupLedger> getOpenRecordsFor(String tradeAccGroupName, String parentSymbol, TradeDirection tradeDirection) {
        return tradeGroupLedgerRepository.findRecordsFor(tradeAccGroupName, TradeStatus.OPEN, parentSymbol, tradeDirection);
    }

    public void createTradeGrpLedgerRecord(
        Map<String, List<TradeAccountConfig>> trdGrpAndAccountsMap,
        TradeDirection tradeDirection,
        @Nullable BigDecimal entryPrice
    ) {
        List<TradeGroupLedger> records = new ArrayList<>();
        for (String trdGrpName : trdGrpAndAccountsMap.keySet()) {
            TradeGroupLedger record = TradeGroupLedger.builder()
                .id(UUID.randomUUID().toString())
                .created(ZonedDateTime.now())
                .lastUpdated(ZonedDateTime.now())
                .tradeAccGroupName(trdGrpName)
                .entryTime(ZonedDateTime.now())
                .tradePlatform(trdGrpAndAccountsMap.get(trdGrpName).get(0).getTradePlatform())
                .entryPrice(entryPrice)
                .tradeStatus(TradeStatus.OPEN)
                .parentSymbol(trdGrpAndAccountsMap.get(trdGrpName).get(0).getParentSymbol())
                .symbol(trdGrpAndAccountsMap.get(trdGrpName).get(0).getSymbol())
                .tradeDirection(tradeDirection)
                .build();
            records.add(record);
        }
        tradeGroupLedgerRepository.saveAll(records);
    }

    public void closeOpenTradesForTradeGrpAndParentSymbol(Set<String> trdGroupsNames, String parentSymbol) {
        for (String grpName : trdGroupsNames) {
            List<TradeGroupLedger> openTrades = tradeGroupLedgerRepository.findByTradeAccGroupNameTradeStatusAndParentSymbol(
                grpName,
                TradeStatus.OPEN,
                parentSymbol
            );
            for (TradeGroupLedger trade : openTrades) {
                trade.setTradeStatus(TradeStatus.CLOSED);
                trade.setExitTime(ZonedDateTime.now());
                trade.setLastUpdated(ZonedDateTime.now());
                tradeGroupLedgerRepository.save(trade);
            }
        }
    }
}

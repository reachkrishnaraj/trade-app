package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.persistance.mongodb.TradeAccountConfig;
import com.kraj.tradeapp.core.repository.mongodb.TradeAccountConfigRepository;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeAccountConfigService {

    private static final String TRADE_ACC_CONFIG_FILE = "classpath:config/trade_acc_config/acc_config.csv";

    private final TradeAccountConfigRepository tradeAccountConfigRepository;
    private final ResourceLoader resourceLoader;

    public Map<String, List<TradeAccountConfig>> getTradeAccountConfigGroupedByTradeGroup(String parentSymbol) {
        List<TradeAccountConfig> accountConfigsForSymbol = tradeAccountConfigRepository.findByParentSymbol(parentSymbol);
        Map<String, List<TradeAccountConfig>> tradeGroupedByTradeGroup = accountConfigsForSymbol
            .stream()
            .filter(TradeAccountConfig::isTradeEnabled)
            .collect(Collectors.groupingBy(TradeAccountConfig::getAccTradeGroupName));
        return tradeGroupedByTradeGroup;
    }

    public void loadTradeAccountConfig() {
        List<TradeAccountConfig> tradeAccountConfigsRaw = readFromCsv(TRADE_ACC_CONFIG_FILE);
        List<TradeAccountConfig> tradeAccountConfigs = tradeAccountConfigsRaw
            .stream()
            .map(tac -> {
                tac.setId(UUID.randomUUID().toString());
                return tac;
            })
            .collect(Collectors.toList());
        tradeAccountConfigRepository.deleteAll();
        tradeAccountConfigRepository.saveAll(tradeAccountConfigs);
    }

    public List<TradeAccountConfig> readFromCsv(String filePath) {
        List<TradeAccountConfig> tradeAccounts = new ArrayList<>();
        Resource resource = resourceLoader.getResource(TRADE_ACC_CONFIG_FILE);
        try (
            Reader reader = new InputStreamReader(resource.getInputStream());
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())
        ) {
            for (CSVRecord record : csvParser) {
                TradeAccountConfig tradeAccount = TradeAccountConfig.builder()
                    .accGroupName(record.get("accGroupName"))
                    .accTradeGroupName(record.get("accTradeGroupName"))
                    .accId(record.get("accId"))
                    .accName(record.get("accName"))
                    .accType(record.get("accType"))
                    .tradePlatform(record.get("tradePlatform"))
                    .pickMyTradeToken(record.get("pickMyTradeToken"))
                    //.symbol(record.get("symbol"))
                    .useTakeProfit(Boolean.parseBoolean(record.get("useTakeProfit")))
                    .takeProfitTicks(Integer.parseInt(record.get("takeProfitTicks")))
                    .useStopLoss(Boolean.parseBoolean(record.get("useStopLoss")))
                    .stopLossTicks(Integer.parseInt(record.get("stopLossTicks")))
                    .useBreakEven(Boolean.parseBoolean(record.get("useBreakEven")))
                    .breakEvenTicks(Integer.parseInt(record.get("breakEvenTicks")))
                    .useTrailingStop(Boolean.parseBoolean(record.get("useTrailingStop")))
                    .trailingStopTicks(Integer.parseInt(record.get("trailingStopTicks")))
                    .quantity(Integer.parseInt(record.get("quantity")))
                    .perTickDollarValue(new java.math.BigDecimal(record.get("perTickDollarValue")))
                    .tradeEnabled(Boolean.parseBoolean(record.get("tradeEnabled")))
                    .automationPlatform(record.get("automationPlatform"))
                    .ownerName(record.get("ownerName"))
                    .ticksPerPoint(Integer.parseInt(record.get("ticksPerPoint")))
                    .parentSymbol(record.get("parentSymbol"))
                    .build();
                tradeAccounts.add(tradeAccount);
            }
        } catch (IOException e) {
            log.error("Error reading trade account config from file: {}", filePath, e);
        }
        return tradeAccounts;
    }
}

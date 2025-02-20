package com.kraj.tradeapp.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kraj.tradeapp.config.TradeAppConfigOptions;
import com.kraj.tradeapp.core.model.CommonUtil;
import com.kraj.tradeapp.core.model.persistance.mongodb.TradeAccountConfig;
import com.kraj.tradeapp.core.model.pickmytrade.TradeAccount;
import com.kraj.tradeapp.core.model.pickmytrade.TradeOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PickMyTradeService {

    private final TradeAppConfigOptions tradeAppConfigOptions;
    //private final TradeAccountConfigService tradeAccountConfigService;
    private final AlertService alertService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient client = new OkHttpClient();

    private TradeOrder getBaseTradeOrder(TradeAccountConfig tradeAccountConfig, String price, String orderDirection) {
        TradeOrder.TradeOrderBuilder builder = TradeOrder.builder();
        builder.symbol(tradeAccountConfig.getSymbol());
        builder.date(CommonUtil.getIsoNowStr());
        builder.quantity(tradeAccountConfig.getQuantity());
        builder.price(price);
        int tpDollar = tradeAccountConfig.isUseTakeProfit()
            ? (int) (tradeAccountConfig.getTakeProfitTicks() * tradeAccountConfig.getTicksPerPoint())
            : 0;
        builder.dollarTp(tpDollar);

        int slDollar = tradeAccountConfig.isUseStopLoss()
            ? (int) (tradeAccountConfig.getStopLossTicks() * tradeAccountConfig.getTicksPerPoint())
            : 0;
        builder.dollarSl(slDollar);

        builder.token(tradeAccountConfig.getPickMyTradeToken());
        builder.orderType("MKT");
        builder.data(orderDirection); //buy or sell or close
        builder.reverseOrderClose(true); //This can also have a value of true or false. If you have a buy position and an open sell order, and you receive a sell alert, it will close the open orders and the position as well. If its value is set to true, it will perform this action; if false, it will not change existing open orders and positions.
        return builder.build();
    }

    private List<TradeAccount> getListOfAccPayload(List<TradeAccountConfig> tradeAccountConfigs, TradeOrder baseTradeOrder) {
        return tradeAccountConfigs
            .stream()
            .map(
                config ->
                    TradeAccount.builder()
                        .accountId(config.getAccId())
                        .token(config.getPickMyTradeToken())
                        .riskPercentage(0)
                        .quantity_multiplier(1)
                        .build()
            )
            .collect(Collectors.toList());
    }

    public boolean placeBuyOrders(String symbol, String price, Map<String, List<TradeAccountConfig>> trdGrpAndAccountsMap) {
        return placeOrdersBase(trdGrpAndAccountsMap, symbol, price, "buy");
    }

    public boolean placeSellOrders(String symbol, String price, Map<String, List<TradeAccountConfig>> trdGrpAndAccountsMap) {
        return placeOrdersBase(trdGrpAndAccountsMap, symbol, price, "sell");
    }

    public boolean placeCloseOrders(String symbol, String price, Map<String, List<TradeAccountConfig>> trdGrpAndAccountsMap) {
        return placeOrdersBase(trdGrpAndAccountsMap, symbol, price, "close");
    }

    public boolean placeOrdersBase(
        Map<String, List<TradeAccountConfig>> trdGrpAndAccountsMap,
        String symbol,
        String price,
        String orderDirection
    ) {
        List<TradeOrder> payloads = getPayload(trdGrpAndAccountsMap, symbol, price, orderDirection);

        List<CompletableFuture<Boolean>> futures = payloads
            .stream()
            .map(tradeOrder ->
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return placeSingleTradeOrder(tradeOrder);
                    } catch (Exception e) {
                        String msg =
                            "Error placing buy order for symbol:%s, tradeGroup:%s, dir:%s, err:%s".formatted(
                                    symbol,
                                    tradeOrder.getMultipleAccounts().get(0).getAccountId(),
                                    orderDirection,
                                    e.getMessage()
                                );
                        alertService.sendTelegramMessage(msg);
                        log.error(msg);
                        return false;
                    }
                }))
            .toList();

        // Wait for all futures to complete and collect results
        List<Boolean> results = futures.stream().map(CompletableFuture::join).toList();

        return results.stream().allMatch(Boolean::booleanValue);
    }

    public List<TradeOrder> getPayload(
        Map<String, List<TradeAccountConfig>> trdGrpAndAccountsMap,
        String symbol,
        String price,
        String orderDirection
    ) {
        List<TradeOrder> tradeOrders = new ArrayList<>();

        for (Map.Entry<String, List<TradeAccountConfig>> entry : trdGrpAndAccountsMap.entrySet()) {
            List<TradeAccountConfig> tradeAccountConfigs = entry.getValue();

            TradeOrder tradeOrder = getBaseTradeOrder(tradeAccountConfigs.get(0), price, orderDirection); //get base trade order

            List<TradeAccount> accounts = getListOfAccPayload(tradeAccountConfigs, tradeOrder);
            tradeOrder.setMultipleAccounts(accounts);
            tradeOrders.add(tradeOrder);
        }
        return tradeOrders;
    }

    public boolean placeSingleTradeOrder(TradeOrder order) throws Exception {
        String payload = objectMapper.writeValueAsString(order);
        String resp = sendPostRequest(tradeAppConfigOptions.getPickMyTradeOrderUrl(), payload);
        log.info("Trade order placed: {}", resp);
        if (StringUtils.containsIgnoreCase(resp, "success")) {
            return true;
        }
        throw new Exception("Error placing pickmytrade order: %s".formatted(resp));
    }

    // Function to send a POST request with JSON body
    public String sendPostRequest(String url, String json) throws Exception {
        RequestBody body = RequestBody.create(json, MediaType.get("application/json"));
        Request request = new Request.Builder().url(url).post(body).header("Content-Type", "application/json").build();

        try (Response response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : "";
        }
    }
}

package com.kraj.tradeapp.core.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.kraj.tradeapp.core.model.TradingReminderConfig;
import com.kraj.tradeapp.core.model.persistance.mongodb.TradeAccountConfig;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleSheetsService {

    private final Sheets googleSheets;

    private static final String APPLICATION_NAME = "Trade Account Config Reader";
    private static final String SPREADSHEET_ID = "1jqHjwMlP1-NQN_f0oetQ8ZVdk8UoN6fsYt55Sbl4a-U";

    public List<TradeAccountConfig> readFromGoogleSheet() {
        try {
            return readFromGoogleSheetPriv();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error reading trade account config from Google Sheets", e);
            throw new RuntimeException(e);
        }
    }

    public List<TradeAccountConfig> readFromGoogleSheetPriv() throws Exception {
        List<TradeAccountConfig> tradeAccounts = new ArrayList<>();

        try {
            // Auto-detect range by fetching all non-empty cells from the first sheet
            String detectedRange = detectAvailableRange(googleSheets, SPREADSHEET_ID, "main_config");
            log.info("Detected range: {}", detectedRange);

            ValueRange response = googleSheets.spreadsheets().values().get(SPREADSHEET_ID, detectedRange).execute();

            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                log.warn("No data found in Google Sheet.");
                return tradeAccounts;
            }

            // Get header row
            List<Object> headers = values.get(0);

            // Iterate over rows (skip first row since it's headers)
            for (int i = 1; i < values.size(); i++) {
                // Skip comment lines
                if (isCommentLine(values.get(i))) {
                    continue;
                }
                List<Object> row = values.get(i);
                TradeAccountConfig tradeAccount = TradeAccountConfig.builder()
                    .accGroupName(getValue(row, headers, "accGroupName"))
                    .accTradeGroupName(getValue(row, headers, "accTradeGroupName"))
                    .accId(getValue(row, headers, "accId"))
                    .accName(getValue(row, headers, "accName"))
                    .accType(getValue(row, headers, "accType"))
                    .tradePlatform(getValue(row, headers, "tradePlatform"))
                    .pickMyTradeToken(getValue(row, headers, "pickMyTradeToken"))
                    .useTakeProfit(Boolean.parseBoolean(getValue(row, headers, "useTakeProfit")))
                    .takeProfitTicks(Integer.parseInt(getValue(row, headers, "takeProfitTicks")))
                    .useStopLoss(Boolean.parseBoolean(getValue(row, headers, "useStopLoss")))
                    .stopLossTicks(Integer.parseInt(getValue(row, headers, "stopLossTicks")))
                    .useBreakEven(Boolean.parseBoolean(getValue(row, headers, "useBreakEven")))
                    .breakEvenTicks(Integer.parseInt(getValue(row, headers, "breakEvenTicks")))
                    .useTrailingStop(Boolean.parseBoolean(getValue(row, headers, "useTrailingStop")))
                    .trailingStopTicks(Integer.parseInt(getValue(row, headers, "trailingStopTicks")))
                    .quantity(Integer.parseInt(getValue(row, headers, "quantity")))
                    .perTickDollarValue(new java.math.BigDecimal(getValue(row, headers, "perTickDollarValue")))
                    .tradeEnabled(Boolean.parseBoolean(getValue(row, headers, "tradeEnabled")))
                    .automationPlatform(getValue(row, headers, "automationPlatform"))
                    .ownerName(getValue(row, headers, "ownerName"))
                    .ticksPerPoint(Integer.parseInt(getValue(row, headers, "ticksPerPoint")))
                    .parentSymbol(getValue(row, headers, "parentSymbol"))
                    .symbol(getValue(row, headers, "symbol"))
                    .build();

                tradeAccounts.add(tradeAccount);
            }
        } catch (Exception e) {
            log.error("Error reading trade account config from Google Sheets", e);
            throw e;
        }
        return tradeAccounts;
    }

    private String getValue(List<Object> row, List<Object> headers, String columnName) {
        int index = headers.indexOf(columnName);
        if (index == -1 || index >= row.size()) return "";
        return row.get(index).toString();
    }

    private boolean isCommentLine(List<Object> row) {
        return row.size() > 0 && row.get(0).toString().startsWith("#");
    }

    /**
     * Detects the available range in the first sheet by getting the last row and last column dynamically.
     */
    private String detectAvailableRange(Sheets sheetsService, String spreadsheetId, String sheetName) throws IOException {
        ValueRange response = sheetsService.spreadsheets().values().get(spreadsheetId, sheetName).execute();

        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            return sheetName + "!A1:A1";
        }

        int lastRow = values.size();
        int lastColumn = values.get(0).size();
        String lastColumnLetter = columnToLetter(lastColumn);

        return "%s!A1:%s%s".formatted(sheetName, lastColumnLetter, lastRow);
    }

    /**
     * Converts column index to letter (e.g., 1 -> A, 2 -> B, 27 -> AA).
     */
    private String columnToLetter(int column) {
        StringBuilder letter = new StringBuilder();
        while (column > 0) {
            column--; // Adjust to 0-based index
            letter.insert(0, (char) ('A' + (column % 26)));
            column /= 26;
        }
        return letter.toString();
    }

    public List<TradingReminderConfig> readRemindersFromSheet() {
        String reminderSheet = "futures_trading_reminders";

        try {
            String detectedRange = detectAvailableRange(googleSheets, SPREADSHEET_ID, reminderSheet);
            log.info("Detected reminder sheet range: {}", detectedRange);

            ValueRange response = googleSheets.spreadsheets().values().get(SPREADSHEET_ID, detectedRange).execute();
            List<List<Object>> values = response.getValues();

            List<TradingReminderConfig> reminders = new ArrayList<>();
            if (values == null || values.isEmpty()) {
                log.warn("No data found in reminder sheet.");
                return reminders;
            }

            List<Object> headers = values.get(0);
            for (int i = 1; i < values.size(); i++) {
                if (isCommentLine(values.get(i))) continue;

                List<Object> row = values.get(i);
                TradingReminderConfig reminder = TradingReminderConfig.builder()
                    .frequency(getValue(row, headers, "frequency"))
                    .cronExpr(getValue(row, headers, "cron_expr"))
                    .message(getValue(row, headers, "message"))
                    .build();

                reminders.add(reminder);
            }

            return reminders;
        } catch (Exception e) {
            log.error("Error reading reminders from Google Sheet", e);
            throw new RuntimeException(e);
        }
    }
}

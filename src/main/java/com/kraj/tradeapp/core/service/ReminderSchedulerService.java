package com.kraj.tradeapp.core.service;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.kraj.tradeapp.core.model.TradingReminderConfig;
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderSchedulerService {

    private final GoogleSheetsService googleSheetsService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${trading.reminder.api.url:}")
    private String apiUrl;

    @Value("${trading.reminder.api.key:}")
    private String apiKey;

    @Value("${trading.reminder.enabled:true}")
    private boolean reminderEnabled;

    private final List<TradingReminderConfig> reminderConfigList = new ArrayList<>();
    private final Set<String> processedReminders = new HashSet<>(); // Prevent duplicate notifications

    @PostConstruct
    public void init() {
        reminderConfigList.clear();
        try {
            reminderConfigList.addAll(googleSheetsService.readRemindersFromSheet());
            log.info("ReminderSchedulerService initialized with {} reminders", reminderConfigList.size());
        } catch (Exception e) {
            log.error("Failed to initialize reminders from Google Sheets: {}", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 60000) // Check every 1 minute
    public void processAllReminders() {
        if (!reminderEnabled) {
            return;
        }

        DateTime currentTime = DateTime.now().withSecondOfMinute(0).withMillisOfSecond(0);
        String currentTimeKey = currentTime.toString("yyyy-MM-dd-HH-mm");

        // Clear processed reminders for new minute
        if (!processedReminders.contains(currentTimeKey)) {
            processedReminders.clear();
        }

        // Process Google Sheets reminders
        processGoogleSheetReminders(currentTime, currentTimeKey);

        // Process hardcoded reminders
        processHardcodedReminders(currentTime, currentTimeKey);
    }

    private void processGoogleSheetReminders(DateTime currentTime, String currentTimeKey) {
        for (TradingReminderConfig reminder : reminderConfigList) {
            String reminderKey = currentTimeKey + "-" + reminder.getFrequency();

            if (!processedReminders.contains(reminderKey)) {
                if (checkCronExpression(reminder.getCronExpr(), currentTime)) {
                    processedReminders.add(reminderKey);

                    String message = reminder.getMessage() != null ? reminder.getMessage() : "Reminder: " + reminder.getFrequency();

                    log.info("Triggered reminder: {} at {}", reminder.getFrequency(), currentTime);
                    sendNotificationAsync(reminder.getFrequency(), reminder.getCronExpr(), message);
                }
            }
        }
    }

    private void processHardcodedReminders(DateTime currentTime, String currentTimeKey) {
        for (Map.Entry<String, String> entry : getICTCronExpressions().entrySet()) {
            String reminderKey = currentTimeKey + "-" + entry.getKey();

            if (!processedReminders.contains(reminderKey)) {
                if (checkCronExpression(entry.getValue(), currentTime)) {
                    processedReminders.add(reminderKey);

                    String message = getICTMessage(entry.getKey());
                    log.info("Triggered ICT reminder: {} at {}", entry.getKey(), currentTime);
                    sendNotificationAsync(entry.getKey(), entry.getValue(), message);
                }
            }
        }
    }

    private boolean checkCronExpression(String cronExpr, DateTime currentTime) {
        try {
            CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
            Cron cron = parser.parse(cronExpr);
            ExecutionTime executionTime = ExecutionTime.forCron(cron);

            // Check if next execution from 1 minute ago equals current time
            DateTime nextFromPast = Optional.ofNullable(executionTime.nextExecution(currentTime.minusMinutes(1))).orElse(null);

            if (nextFromPast != null && nextFromPast.equals(currentTime)) {
                return true;
            }

            // Alternative check: if last execution was exactly at current time
            DateTime lastExecution = Optional.ofNullable(executionTime.lastExecution(currentTime.plusMinutes(1))).orElse(null);

            return lastExecution != null && lastExecution.equals(currentTime);
        } catch (Exception e) {
            log.error("Error parsing cron expression '{}': {}", cronExpr, e.getMessage());
            return false;
        }
    }

    private void sendNotificationAsync(String frequency, String cronExpr, String message) {
        if (apiUrl == null || apiUrl.trim().isEmpty()) {
            log.warn("API URL not configured. Skipping notification for: {}", frequency);
            return;
        }

        CompletableFuture.runAsync(() -> sendNotification(frequency, cronExpr, message)).exceptionally(throwable -> {
            log.error("Async notification failed for {}: {}", frequency, throwable.getMessage());
            return null;
        });
    }

    private void sendNotification(String frequency, String cronExpr, String message) {
        try {
            // Create request payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("frequency", frequency);
            payload.put("cron_expression", cronExpr);
            payload.put("message", message);
            payload.put("timestamp", DateTime.now().toString());
            payload.put("timezone", "EST");
            payload.put("type", "trading_reminder");

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            if (apiKey != null && !apiKey.trim().isEmpty()) {
                headers.set("Authorization", "Bearer " + apiKey);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // Send POST request
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully sent notification: {}", frequency);
            } else {
                log.warn("Failed to send notification for {}. Status: {}", frequency, response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending notification for {}: {}", frequency, e.getMessage());
        }
    }

    private String getICTMessage(String frequency) {
        Map<String, String> messages = new HashMap<>();

        // ICT Macro Messages
        messages.put("London Macro 1 Alert", "London Macro 1 starting in 1 minute (02:33-03:00 AM EST)");
        messages.put("London Macro 2 Alert", "London Macro 2 starting in 1 minute (04:03-04:30 AM EST)");
        messages.put("NY AM Macro 1 Alert", "NY AM Macro 1 starting in 1 minute (08:50-09:10 AM EST)");
        messages.put("NY AM Macro 2 Alert", "NY AM Macro 2 starting in 1 minute (09:50-10:10 AM EST)");
        messages.put("NY AM Macro 3 Alert", "NY AM Macro 3 starting in 1 minute (10:50-11:10 AM EST)");
        messages.put("NY Lunch Macro Alert", "NY Lunch Macro starting in 1 minute (11:50 AM-12:10 PM EST)");
        messages.put("NY PM Macro Alert", "NY PM Macro starting in 1 minute (01:10-01:40 PM EST)");
        messages.put("NY Last Hour Macro Alert", "NY Last Hour Macro starting in 1 minute (03:15-03:45 PM EST)");

        // 4H Candle Messages
        messages.put(
            "2AM Alert",
            "New 4H candle open - Look for POI SMT expect sweep inversion in wick or continuation. Check prev 4H candle for bias"
        );
        messages.put(
            "6AM Alert",
            "New 4H candle open - Look for POI SMT expect sweep inversion in wick or continuation. Check prev 4H candle for bias"
        );
        messages.put(
            "10AM Alert",
            "New 4H candle open - Look for POI SMT expect sweep inversion in wick or continuation. Check prev 4H candle for bias"
        );
        messages.put(
            "2PM Alert",
            "New 4H candle open - Look for POI SMT expect sweep inversion in wick or continuation. Check prev 4H candle for bias"
        );
        messages.put(
            "6PM Alert",
            "New 4H candle open - Look for POI SMT expect sweep inversion in wick or continuation. Check prev 4H candle for bias"
        );
        messages.put(
            "10PM Alert",
            "New 4H candle open - Look for POI SMT expect sweep inversion in wick or continuation. Check prev 4H candle for bias"
        );

        // Kill Zone Messages
        messages.put("Asian Kill Zone Start", "Asian Kill Zone STARTED (08:00 PM - 10:00 PM EST)");
        messages.put("Asian Kill Zone End", "Asian Kill Zone ENDED (08:00 PM - 10:00 PM EST)");
        messages.put("London Kill Zone Start", "London Kill Zone STARTED (02:00 AM - 05:00 AM EST)");
        messages.put("London Kill Zone End", "London Kill Zone ENDED (02:00 AM - 05:00 AM EST)");
        messages.put("NY Kill Zone Start", "New York Kill Zone STARTED (08:00 AM - 11:00 AM EST)");
        messages.put("NY Kill Zone End", "New York Kill Zone ENDED (08:00 AM - 11:00 AM EST)");
        messages.put("London Close Kill Zone Start", "London Close Kill Zone STARTED (10:00 AM - 12:00 PM EST)");
        messages.put("London Close Kill Zone End", "London Close Kill Zone ENDED (10:00 AM - 12:00 PM EST)");

        return messages.getOrDefault(frequency, "Trading reminder: " + frequency);
    }

    private Map<String, String> getICTCronExpressions() {
        Map<String, String> cronMap = new HashMap<>();

        // ICT Macro Times - 1 minute before
        cronMap.put("London Macro 1 Alert", "32 2 * * *");
        cronMap.put("London Macro 2 Alert", "2 4 * * *");
        cronMap.put("NY AM Macro 1 Alert", "49 8 * * *");
        cronMap.put("NY AM Macro 2 Alert", "49 9 * * *");
        cronMap.put("NY AM Macro 3 Alert", "49 10 * * *");
        cronMap.put("NY Lunch Macro Alert", "49 11 * * *");
        cronMap.put("NY PM Macro Alert", "9 13 * * *");
        cronMap.put("NY Last Hour Macro Alert", "14 15 * * *");

        // 4H Candle Alerts
        cronMap.put("2AM Alert", "0 2 * * *");
        cronMap.put("6AM Alert", "0 6 * * *");
        cronMap.put("10AM Alert", "0 10 * * *");
        cronMap.put("2PM Alert", "0 14 * * *");
        cronMap.put("6PM Alert", "0 18 * * *");
        cronMap.put("10PM Alert", "0 22 * * *");

        // Kill Zones
        cronMap.put("Asian Kill Zone Start", "0 20 * * *");
        cronMap.put("Asian Kill Zone End", "0 22 * * *");
        cronMap.put("London Kill Zone Start", "0 2 * * *");
        cronMap.put("London Kill Zone End", "0 5 * * *");
        cronMap.put("NY Kill Zone Start", "0 8 * * *");
        cronMap.put("NY Kill Zone End", "0 11 * * *");
        cronMap.put("London Close Kill Zone Start", "0 10 * * *");
        cronMap.put("London Close Kill Zone End", "0 12 * * *");

        return cronMap;
    }

    // Manual refresh method for Google Sheets data
    public void refreshReminders() {
        init();
        log.info("Reminders refreshed manually");
    }

    // Get current reminder count for monitoring
    public int getReminderCount() {
        return reminderConfigList.size() + getICTCronExpressions().size();
    }
}

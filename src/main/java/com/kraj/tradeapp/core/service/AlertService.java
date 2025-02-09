package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.PayloadKey;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final TwilioService twilioService;
    private final TelegramBotConfig telegramBotConfig;
    private final NotificationProcessorService notificationProcessorService;

    private List<String> chatIds = List.of("5006562667");

    //payload: symbol=NQ|price=1234.5|msg=Price reached target test|textNums=+917305989831,+14083486083|voiceNums=+917305989831,+14083486083
    public void handleDirectTradingViewAlert(String message) {
        Map<String, String> msgMap = notificationProcessorService.getPayloadMap(message);
        String symbol = notificationProcessorService
            .getValueFor(PayloadKey.SYMBOL, msgMap)
            .orElseThrow(() -> new RuntimeException("Symbol not found in payload"));
        String price = notificationProcessorService
            .getValueFor(PayloadKey.PRICE, msgMap)
            .orElseThrow(() -> new RuntimeException("Price not found in payload"));
        String alertMsg = notificationProcessorService
            .getValueFor(PayloadKey.ALERT_MESSAGE, msgMap)
            .orElseThrow(() -> new RuntimeException("Symbol not found in payload"));
        @Nullable
        String[] textNumbers = notificationProcessorService
            .getValueFor(PayloadKey.TEXT_NUMBER, msgMap)
            .filter(StringUtils::isNotBlank)
            .map(s -> s.split(","))
            .orElse(null);
        @Nullable
        String[] voiceNumbers = notificationProcessorService
            .getValueFor(PayloadKey.VOICE_NUMBER, msgMap)
            .filter(StringUtils::isNotBlank)
            .map(s -> s.split(","))
            .orElse(null);
        String msgToSend = "[%s] [%s] [%s]".formatted(symbol, price, alertMsg);

        if (textNumbers != null) {
            for (String number : textNumbers) {
                sendMessage(() -> twilioService.sendTextMessage(msgToSend, StringUtils.trim(number)), "text", number);
                sendTelegramMessage(msgToSend);
            }
        }

        if (voiceNumbers != null) {
            for (String number : voiceNumbers) {
                sendMessage(() -> twilioService.sendVoiceMessage(msgToSend, StringUtils.trim(number)), "voice", number);
            }
        }
    }

    private void sendMessage(Runnable action, String type, String number) {
        try {
            action.run();
        } catch (Exception e) {
            System.err.println("Failed to send " + type + " message to " + number + ": " + e.getMessage());
        }
    }

    public void sendTelegramMessage(String message) {
        try {
            for (String chatId : chatIds) {
                TelegramBotConfig.telegramBot.execute(new SendMessage(chatId, message));
            }
        } catch (Exception e) {
            System.err.println("Failed to send telegram message: " + e.getMessage());
        }
    }
}

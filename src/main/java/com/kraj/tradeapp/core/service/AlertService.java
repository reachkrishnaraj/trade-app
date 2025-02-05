package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.core.model.PayloadKey;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final TwilioService twilioService;
    private final NotificationProcessorService notificationProcessorService;

    //    public void handleDirectTradingViewAlert(String message) {
    //        Map<String,String> msgMap = notificationProcessorService.getPayloadMap(message);
    //        String symbol = notificationProcessorService.getValueFor(PayloadKey.SYMBOL, msgMap).orElseThrow(() -> new RuntimeException("Symbol not found in payload"));
    //        String price = notificationProcessorService.getValueFor(PayloadKey.PRICE, msgMap).orElseThrow(() -> new RuntimeException("Price not found in payload"));
    //        String alertMsg = notificationProcessorService.getValueFor(PayloadKey.ALERT_MESSAGE, msgMap).orElseThrow(() -> new RuntimeException("Symbol not found in payload"));
    //        String symbol = notificationProcessorService.getValueFor(PayloadKey.SYMBOL, msgMap).orElseThrow(() -> new RuntimeException("Symbol not found in payload"));
    //        String symbol = notificationProcessorService.getValueFor(PayloadKey.SYMBOL, msgMap).orElseThrow(() -> new RuntimeException("Symbol not found in payload"));
    //        String messageToSend = msgMap.get("message");
    //    }

    public void handleDirectTradingViewAlertSimple() {
        twilioService.sendVoiceMessage("Price reached target", "+917305989831");
        twilioService.sendTextMessage("Price reached target", "+917305989831");

        twilioService.sendVoiceMessage("Price reached target", "+14083486083");
        twilioService.sendTextMessage("Price reached target", "+14083486083");
    }
}

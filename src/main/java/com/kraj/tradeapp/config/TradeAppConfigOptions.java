package com.kraj.tradeapp.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "trade.app.options")
@Configuration
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeAppConfigOptions {

    private String twilioAccountSid;

    private String twilioAuthToken;

    private String twilioPhoneNumber;

    private String pathFlowSid;

    private String notificationPhoneNumbers;

    private String twilioVoiceXmlUrl;

    private String pickMyTradeOrderUrl;

    private String googleSheetServiceAccCredJson;
}

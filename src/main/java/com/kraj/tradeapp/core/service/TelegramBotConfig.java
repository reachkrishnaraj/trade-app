package com.kraj.tradeapp.core.service;

import com.github.kshashov.telegram.config.TelegramBotGlobalProperties;
import com.github.kshashov.telegram.config.TelegramBotGlobalPropertiesConfiguration;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
public class TelegramBotConfig implements TelegramBotGlobalPropertiesConfiguration {

    @Getter
    @Value("${bot.token}")
    private String token;

    public static TelegramBot telegramBot;

    private List<String> chatIds = List.of("5006562667");

    @Override
    public void configure(TelegramBotGlobalProperties.Builder builder) {
        builder.processBot(token, bot -> {
            telegramBot = bot;
        });
    }

    public void sendMessageToDefaultBotAllChatIds(String message) {
        chatIds.forEach(chatId -> {
            telegramBot.execute(new SendMessage(chatId, message));
        });
    }
}

package com.kraj.tradeapp.core.controller;

import com.github.kshashov.telegram.api.TelegramMvcController;
import com.github.kshashov.telegram.api.bind.annotation.BotController;
import org.springframework.beans.factory.annotation.Value;

@BotController
public class TelegramBotController implements TelegramMvcController {

    @Value("${bot.token}")
    private String token;

    @Override
    public String getToken() {
        return token;
    }
}

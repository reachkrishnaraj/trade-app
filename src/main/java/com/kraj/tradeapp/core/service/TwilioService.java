package com.kraj.tradeapp.core.service;

import com.kraj.tradeapp.config.TradeAppConfigOptions;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;
import com.twilio.type.PhoneNumber;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TwilioService {

    private final TradeAppConfigOptions tradeAppConfigOptions;

    public void sendVoiceMessage(String message, String phoneNumber) {
        Twilio.init(tradeAppConfigOptions.getTwilioAccountSid(), tradeAppConfigOptions.getTwilioAuthToken());

        Call call = Call.creator(
            new PhoneNumber(phoneNumber), // To
            new PhoneNumber(tradeAppConfigOptions.getTwilioPhoneNumber()), // Twilio number
            URI.create("http://demo.twilio.com/docs/voice.xml")
            //URI.create(tradeAppConfigOptions.getTwilioVoiceXmlUrl() + "/This_is_test_message") // TwiML URL
        ).create();
    }

    public void sendTextMessage(String msgStr, String phoneNumber) {
        Twilio.init(tradeAppConfigOptions.getTwilioAccountSid(), tradeAppConfigOptions.getTwilioAuthToken());
        Message message = Message.creator(
            new com.twilio.type.PhoneNumber(phoneNumber),
            new com.twilio.type.PhoneNumber(tradeAppConfigOptions.getTwilioPhoneNumber()),
            msgStr
        ).create();
    }
}

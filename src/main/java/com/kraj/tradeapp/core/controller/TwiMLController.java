package com.kraj.tradeapp.core.controller;

import com.kraj.tradeapp.core.service.TwilioService;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Say;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TwiMLController {

    private final TwilioService twilioService;

    @GetMapping(value = "/api/v1/twiml/voice/{message}", produces = "application/xml")
    public ResponseEntity<?> getTwiML(@PathVariable String message) {
        String modifiedMsg = message.replace("_", " ");
        VoiceResponse response = new VoiceResponse.Builder().say(new Say.Builder(modifiedMsg).voice(Say.Voice.ALICE).build()).build();
        return ResponseEntity.ok(response.toXml());
    }

    @GetMapping("/api/v1/testVoiceMsg")
    public void testVoiceMsg() {
        twilioService.sendVoiceMessage("This is test", "+917305989831");
    }
}

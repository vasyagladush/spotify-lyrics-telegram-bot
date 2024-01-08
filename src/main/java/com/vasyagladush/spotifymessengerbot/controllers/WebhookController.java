package com.vasyagladush.spotifymessengerbot.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.vasyagladush.spotifymessengerbot.messengers.telegram.TelegramBot;

@RestController
@RequestMapping("/webhook")
public class WebhookController {
    private static final Logger logger = LogManager.getLogger(TelegramBot.class);
    private final TelegramBot telegramBot;

    @Autowired
    public WebhookController(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostMapping("/telegram/")
    public ResponseEntity<?> onTelegramWebhookReceived(@RequestBody Update update,
            @RequestHeader("X-Telegram-Bot-Api-Secret-Token") String secretToken) {
        logger.debug("Telegram: new webhook received");
        if (!secretToken.equals(telegramBot.getWebhookSecretToken())) {
            logger.warn("Telegram: unauthorized webhook");
            return ResponseEntity.status(HttpStatus.FORBIDDEN_403.getStatusCode()).build();
        } else {
            if (update != null && update.hasMessage()) {
                telegramBot.onWebhookUpdateReceived(update);
            }
            return ResponseEntity.ok().build();
        }
    }

    // @PostMapping("/{messengerPlatform}")
    // public ResponseEntity<?> processPost(@PathVariable("messengerPlatform")
    // String messengerPlatformEndpoint,
    // @RequestBody RequestBody requestBody,
    // @RequestHeader HttpHeaders headers) {
    // System.out.println(ServletUriComponentsBuilder.fromCurrentRequestUri().toUriString());
    // try {
    // // Enum.valueOf() throws an exception if the input is not valid
    // MessengerPlatformWebhookEndpoint endpoint = MessengerPlatformWebhookEndpoint
    // .valueOf(messengerPlatformEndpoint);

    // switch (endpoint) {
    // case TELEGRAM:
    // return onTelegramUpdateReceived((Update) requestBody, "adad");
    // default:
    // return ResponseEntity.notFound().build();
    // }
    // } catch (IllegalArgumentException e) {
    // return ResponseEntity.notFound().build();
    // }
    // }
}
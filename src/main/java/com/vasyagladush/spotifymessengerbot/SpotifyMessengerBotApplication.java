package com.vasyagladush.spotifymessengerbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.vasyagladush.spotifymessengerbot.messengers.telegram.TelegramBot;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class SpotifyMessengerBotApplication {
	private static final Logger logger = LogManager.getLogger(TelegramBot.class);

	@Autowired
	private TelegramBot telegramBot;

	public static void main(String[] args) {
		SpringApplication.run(SpotifyMessengerBotApplication.class, args);
	}

	@PostConstruct
	public void registerTelegramBot() {
		try {
			logger.info("Starting Telegram Bot set up");
			SetWebhook setWebhook = SetWebhook.builder().url(telegramBot.getWebhookBaseUrl())
					.secretToken(telegramBot.getWebhookSecretToken()).build();
			telegramBot.onRegister();
			telegramBot.setWebhook(setWebhook);
			logger.info(
					"Successfully registered Telegram Bot, with webhook URL: " + telegramBot.getWebhookBaseUrl());
		} catch (TelegramApiException e) {
			logger.error("Error setting up Telegram Bot");
			logger.trace(e.getStackTrace());
		}
	}
}

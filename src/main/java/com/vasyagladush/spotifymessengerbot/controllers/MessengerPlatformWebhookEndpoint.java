package com.vasyagladush.spotifymessengerbot.controllers;

public enum MessengerPlatformWebhookEndpoint {
    TELEGRAM("telegram");

    private final String value;

    MessengerPlatformWebhookEndpoint(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
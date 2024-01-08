package com.vasyagladush.spotifymessengerbot.controllers;

import java.io.IOException;
import java.util.Date;
import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.vasyagladush.spotifymessengerbot.messengers.telegram.TelegramBot;
import com.vasyagladush.spotifymessengerbot.musicproviders.spotify.SpotifyService;
import com.vasyagladush.spotifymessengerbot.musicproviders.spotify.types.SpotifyAccessTokenGrantedResponse;
import com.vasyagladush.spotifymessengerbot.services.UserService;

@RestController
@RequestMapping("/callback")
public class CallbackController {
    private static final Logger logger = LogManager.getLogger(TelegramBot.class);

    private final UserService userService;
    private final SpotifyService spotifyService;

    @Autowired
    public CallbackController(UserService userService, SpotifyService spotifyService) {
        this.userService = userService;
        this.spotifyService = spotifyService;
    }

    @GetMapping("/spotify/")
    @ResponseBody
    public String onSpotifyCallback(@RequestParam("code") final String code,
            @RequestParam("state") final String state) {
        try {
            logger.info("Spotify: callback url hit, state: " + state);
            final SpotifyAccessTokenGrantedResponse accessTokenResponse = this.spotifyService
                    .exchangeCodeOnAccessToken(code, state);
            final Date now = new Date();
            this.userService.updateWithMusicProviderAccessTokens(state, accessTokenResponse.access_token,
                    accessTokenResponse.refresh_token, new Date(now.getTime() + accessTokenResponse.expires_in * 1000));
            return "Spotify authorized. You can return to the chat now.";
        } catch (NoSuchElementException e) {
            logger.error("Spotify: callback url, no such user found, state: " + state);
            return "User couldn't be found. Please try following the authorization process from the start from your messenger platform.";
        } catch (IOException e) {
            logger.error("Spotify: callback url, error message: " + e.getMessage());
            logger.trace(e.getStackTrace());
            return "Something went wrong. Please try again.";
        }
    }
}
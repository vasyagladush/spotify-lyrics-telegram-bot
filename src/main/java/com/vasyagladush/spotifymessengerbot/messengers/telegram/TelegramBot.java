package com.vasyagladush.spotifymessengerbot.messengers.telegram;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.vasyagladush.spotifymessengerbot.lyricsproviders.genius.GeniusService;
import com.vasyagladush.spotifymessengerbot.models.MessengerPlatform;
import com.vasyagladush.spotifymessengerbot.models.MusicProviderPlatform;
import com.vasyagladush.spotifymessengerbot.models.User;
import com.vasyagladush.spotifymessengerbot.musicproviders.spotify.SpotifyService;
import com.vasyagladush.spotifymessengerbot.musicproviders.spotify.types.SpotifyGetCurrentlyPlayingTrackResponse;
import com.vasyagladush.spotifymessengerbot.musicproviders.spotify.types.SpotifyGetCurrentlyPlayingTrackResponse.Artist;
import com.vasyagladush.spotifymessengerbot.services.UserService;

@Component
public class TelegramBot extends TelegramWebhookBot {
    private static final String[] AUTH_MESSAGE_INPUTS = { "/start" };
    private static final String[] LYRICS_MESSAGE_INPUTS = { "Lyrics", "/lyrics", };
    private static final String[] CLEAR_MESSAGE_INPUTS = { "Clear", "/clear", };
    private static final String[] OPEN_SETTINGS_MESSAGE_INPUTS = { "Settings" };
    private static final String[] CLOSE_SETTINGS_INPUTS = { "Leave Settings" };
    private static final String[] TOGGLE_AUTOCLEAR_INPUTS = { "Autoclear", "/autoclear" };

    private static final ReplyKeyboardMarkup DEFAULT_REPLY_KEYBOARD_MARKUP = TelegramBot
            .constructDefaultReplyKeyboardMarkup();

    private static final Logger logger = LogManager.getLogger(TelegramBot.class);

    private final String botToken;
    private final String botUsername;
    private final String serverBaseUrl;
    private final String webhookSecretToken;
    private final UserService userService;
    private final SpotifyService spotifyService;
    private final GeniusService geniusService;

    @Autowired
    public TelegramBot(@Value("${TELEGRAM_BOT_TOKEN}") String botToken,
            @Value("${TELEGRAM_BOT_USERNAME}") String botUsername, @Value("${BASE_URL}") String baseUrl,
            @Value("${TELEGRAM_WEBHOOK_SECRET_TOKEN}") String webhookSecretToken, UserService userService,
            SpotifyService spotifyService, GeniusService geniusService) {
        super(botToken);
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.serverBaseUrl = baseUrl;
        this.webhookSecretToken = webhookSecretToken;
        this.userService = userService;
        this.spotifyService = spotifyService;
        this.geniusService = geniusService;
    }

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public String getBotPath() {
        return "";
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }

    public String getWebhookBaseUrl() {
        String webhookBaseUrl = this.serverBaseUrl;
        if (!webhookBaseUrl.endsWith("/")) {
            webhookBaseUrl += "/";
        }
        webhookBaseUrl += "webhook/telegram/";
        return webhookBaseUrl;
    }

    public String getWebhookSecretToken() {
        return this.webhookSecretToken;
    }

    @Override
    public void setWebhook(SetWebhook setWebhook) throws TelegramApiException {
        WebhookUtilsV2.setWebhook(this, this, setWebhook);
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(final Update update) {
        final String chatId = update.getMessage().getChatId().toString();
        final User user = this.userService.createOrUpdate(MessengerPlatform.TELEGRAM, chatId,
                MusicProviderPlatform.SPOTIFY);

        logger.info("Platform: " + MessengerPlatform.TELEGRAM + ": message received from chat id " + chatId);
        logger.debug("Platform: " + MessengerPlatform.TELEGRAM + ": message received from chat id " + chatId +
                ", messageId: " + update.getMessage().getMessageId().toString());

        try {
            userService.addMessageToClear(user, update.getMessage().getMessageId().toString());

            if (update.hasMessage() && update.getMessage().hasText()) {
                String messageText = update.getMessage().getText().trim();

                if (Arrays.stream(TelegramBot.AUTH_MESSAGE_INPUTS).anyMatch(messageText::equals)) {
                    this.processAuthorizationRequestMessage(user, chatId);
                }

                else if (Arrays.stream(TelegramBot.LYRICS_MESSAGE_INPUTS).anyMatch(messageText::equals)) {
                    if (user.isAutoclear())
                        this.clearMessagesMarkedAsToClear(chatId, user);
                    this.processLyricsRequestMessage(user, chatId);
                }

                else if (Arrays.stream(TelegramBot.CLEAR_MESSAGE_INPUTS).anyMatch(messageText::equals)) {
                    this.clearMessagesMarkedAsToClear(chatId, user);
                }

                else if (Arrays.stream(TelegramBot.OPEN_SETTINGS_MESSAGE_INPUTS).anyMatch(messageText::equals)) {
                    this.sendResponseMessage(chatId, user, "Please configure the settings now",
                            constructSettingsReplyKeyboardMarkup(user));
                }

                else if (Arrays.stream(TelegramBot.CLOSE_SETTINGS_INPUTS).anyMatch(messageText::equals)) {
                    this.sendResponseMessage(chatId, user, "Settings closed",
                            TelegramBot.DEFAULT_REPLY_KEYBOARD_MARKUP);
                }

                else if (Arrays.stream(TelegramBot.TOGGLE_AUTOCLEAR_INPUTS).anyMatch(messageText::startsWith)) {
                    userService.toggleAutoclear(user);
                    this.sendResponseMessage(chatId, user,
                            "Autoclear is turned " + (user.isAutoclear() ? "on" : "off"),
                            constructSettingsReplyKeyboardMarkup(user));
                }

                else {
                    this.sendResponseMessage(chatId, user, "Unprocessable input");
                }
            } else {
                this.sendResponseMessage(chatId, user, "Error: no text input");
            }

            return null;
        } catch (Throwable e) {
            logger.error("Platform: " + MessengerPlatform.TELEGRAM + ": error with chat: " + chatId
                    + ", error message: " + e.getMessage());
            logger.trace(e.getStackTrace());
            try {
                this.sendResponseMessage(chatId, user,
                        "An unexpected error occured. Please try again. In case the error keeps persisting, try following the authorization process again: /start");
            } catch (Throwable e2) {
                logger.error("Platform: " + MessengerPlatform.TELEGRAM + ": error with chat: " + chatId
                        + ", error message: " + e2.getMessage());
                logger.trace(e2.getStackTrace());
            }
        }

        return null;
    }

    private Message sendResponseMessage(final String chatId, final User user, final String messageContent,
            final ReplyKeyboardMarkup replyKeyboardMarkup)
            throws TelegramApiException, JsonProcessingException, JsonMappingException {
        final Message responseMessage = this.execute(SendMessage.builder().chatId(chatId).text(messageContent)
                .replyMarkup(replyKeyboardMarkup).parseMode(ParseMode.MARKDOWN).build());

        userService.addMessageToClear(user, responseMessage.getMessageId().toString());

        return responseMessage;
    }

    private Message sendResponseMessage(final String chatId, final User user, final String messageContent)
            throws TelegramApiException, JsonProcessingException, JsonMappingException {
        return this.sendResponseMessage(chatId, user, messageContent, TelegramBot.DEFAULT_REPLY_KEYBOARD_MARKUP);
    }

    private void clearMessagesMarkedAsToClear(final String chatId, final User user)
            throws TelegramApiException, JsonProcessingException, JsonMappingException {
        logger.debug("inside clearMessagesMarkedAsToClear");

        userService.getMessagesToClearAsArrayListOfString(user)
                .forEach((messageId) -> this.safeDeleteMessage(new DeleteMessage(chatId, Integer.valueOf(messageId))));
        userService.resetMessagesToClear(user);
    }

    private boolean safeDeleteMessage(DeleteMessage deleteMessageAction) {
        logger.debug("inside safeDeleteMessage");
        logger.debug("delete messageId: " + deleteMessageAction.getMessageId() + ", chatId: "
                + deleteMessageAction.getChatId());
        try {
            return this.execute(deleteMessageAction);
        } catch (TelegramApiException e) {
            logger.error("Platform: " + MessengerPlatform.TELEGRAM
                    + ", error message: " + e.getMessage());
            logger.trace(e.getStackTrace());

            return false;
        }
    }

    // TODO: in future, when there's not only Spotify, add musicProviderPlatform
    // argument
    private void processAuthorizationRequestMessage(final User user, final String chatId)
            throws TelegramApiException, JsonMappingException, JsonProcessingException {
        this.sendResponseMessage(chatId, user,
                "Please follow the [link](" + spotifyService.constructAuthorizationLink(user.getId().toString())
                        + ") to authorize Spotify\n"
                        + "\nAfter you authorize Spotify, just send /lyrics command or type in \"Lyrics\" to get them\n\n");
    }

    // TODO: in future, when there's not only Spotify, add musicProviderPlatform
    // argument
    private void processLyricsRequestMessage(final User user, final String chatId)
            throws JsonProcessingException, JsonMappingException, ClientProtocolException, IOException,
            TelegramApiException {
        SpotifyGetCurrentlyPlayingTrackResponse currentlyPlayingTrack = spotifyService
                .getCurrentlyPlayingTrack(user);

        if (currentlyPlayingTrack == null) {
            this.sendResponseMessage(chatId, user, "No song is currently playing");
            return;
        }

        final String songName = currentlyPlayingTrack.getItem().getName();
        final Artist[] artists = currentlyPlayingTrack.getItem().getArtists();

        String artistNames = "";
        for (int i = 0; i < artists.length; ++i) {
            if (i == artists.length - 1) {
                artistNames += artists[i].getName();
            } else {
                artistNames += artists[i].getName() + ", ";
            }
        }

        String songInfoMessage = "";

        if (artists.length > 1) {
            songInfoMessage = String.format("Song: %s\nArtists: %s", songName, artistNames);
        } else {
            songInfoMessage = String.format("Song: %s\nArtist: %s", songName, artistNames);
        }

        this.sendResponseMessage(chatId, user, songInfoMessage);

        try {
            final String lyrics = this.geniusService.getSongLyrics(songName, artistNames);
            this.sendResponseMessage(chatId, user, lyrics);

        } catch (IndexOutOfBoundsException noLyricsException) {
            logger.debug("Platform: " + MessengerPlatform.TELEGRAM + ": no lyrics found");
            this.sendResponseMessage(chatId, user, "No lyrics found for this song");
        } catch (IOException lyricsFetchException) {
            logger.error("Platform: " + MessengerPlatform.TELEGRAM + ": error with chat: " + chatId
                    + ", error fetchingg lyrics, error message: " + lyricsFetchException.getMessage());
            logger.trace(lyricsFetchException.getStackTrace());
            this.sendResponseMessage(chatId, user, "Error occured while trying to find lyrics");
        }
    }

    private static ReplyKeyboardMarkup constructDefaultReplyKeyboardMarkup() {
        // Create ReplyKeyboardMarkup object
        final ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        // Create the keyboard (list of keyboard rows)
        final List<KeyboardRow> keyboard = new ArrayList<>();

        // Create a keyboard row
        KeyboardRow keyboardRow = new KeyboardRow();
        // Set each button, you can also use KeyboardButton objects if you need
        // something else than text
        keyboardRow.add(LYRICS_MESSAGE_INPUTS[0]);
        keyboardRow.add(CLEAR_MESSAGE_INPUTS[0]);
        keyboardRow.add(OPEN_SETTINGS_MESSAGE_INPUTS[0]);

        // Add the first row to the keyboard
        keyboard.add(keyboardRow);

        // Set the keyboard to the markup
        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }

    private static ReplyKeyboardMarkup constructSettingsReplyKeyboardMarkup(User user) {
        final ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);

        final List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardRow = new KeyboardRow();

        keyboardRow.add(CLOSE_SETTINGS_INPUTS[0]);
        keyboardRow.add(TOGGLE_AUTOCLEAR_INPUTS[0] + (user.isAutoclear() ? " Off" : " On"));

        keyboard.add(keyboardRow);

        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }
}

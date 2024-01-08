package com.vasyagladush.spotifymessengerbot.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasyagladush.spotifymessengerbot.models.MessengerPlatform;
import com.vasyagladush.spotifymessengerbot.models.MusicProviderPlatform;
import com.vasyagladush.spotifymessengerbot.models.User;
import com.vasyagladush.spotifymessengerbot.repositories.UserRepository;

@Service
public class UserService {
    private final UserRepository repository;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(UserService.class);

    @Autowired
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User createOrUpdate(final MessengerPlatform messengerPlatform, final String messengerUserId,
            final MusicProviderPlatform musicProviderPlatform) {
        final Optional<User> existingUserOptional = this.get(messengerPlatform, messengerUserId, musicProviderPlatform);
        if (existingUserOptional.isPresent()) {
            final User existingUser = existingUserOptional.get();
            existingUser.setMusicProviderPlatform(musicProviderPlatform.name());
            return existingUser;
        }

        User newUser = new User();
        newUser.setMessengerPlatform(messengerPlatform.name());
        newUser.setMessengerUserId(messengerUserId);
        newUser.setMusicProviderPlatform(musicProviderPlatform.name());
        repository.save(newUser);
        return newUser;
    }

    public User updateWithMusicProviderAccessTokens(final String messengerUserId,
            final String accessToken, final String refreshToken,
            final Date accessTokenExpiresAt) throws NoSuchElementException {
        final User user = this.get(UUID.fromString(messengerUserId)).get();
        user.setMusicProviderAccessToken(accessToken);
        user.setMusicProviderAccessTokenExpiresAt(accessTokenExpiresAt);
        user.setMusicProviderRefreshToken(refreshToken);
        repository.save(user);
        return user;
    }

    public User updateWithMusicProviderAccessTokens(final User user,
            final String accessToken, final String refreshToken,
            final Date accessTokenExpiresAt) {
        user.setMusicProviderAccessToken(accessToken);
        user.setMusicProviderAccessTokenExpiresAt(accessTokenExpiresAt);
        if (refreshToken != null) {
            user.setMusicProviderRefreshToken(refreshToken);
        }
        repository.save(user);
        return user;
    }

    public Optional<User> get(final UUID id) {
        return repository.findById(id);
    }

    public Optional<User> get(final MessengerPlatform messengerPlatform, final String messengerUserId,
            final MusicProviderPlatform musicProviderPlatform) {
        return repository.findFirstByMessengerPlatformAndMessengerUserIdAndMusicProviderPlatform(
                messengerPlatform.name(), messengerUserId, musicProviderPlatform.name());
    }

    public ArrayList<String> getMessagesToClearAsArrayListOfString(final User user)
            throws JsonProcessingException, JsonMappingException {
        final String messagesToClear = user.getMessagesToClear();

        if (messagesToClear == null || messagesToClear.equals("[]"))
            return new ArrayList<String>();

        return new ArrayList<String>(Arrays.asList(objectMapper.readValue(messagesToClear, String[].class)));
    }

    public void addMessageToClear(final User user, final String messageId)
            throws JsonProcessingException, JsonMappingException {
        logger.debug("inside addMessageToClear");

        final ArrayList<String> messagesToClearArrayList = this.getMessagesToClearAsArrayListOfString(user);
        logger.debug("before .add() messagesToClearArrayList: " + messagesToClearArrayList.toString());

        messagesToClearArrayList.add(messageId);
        logger.debug("after .add() messagesToClearArrayList: " + messagesToClearArrayList.toString());

        final String[] newMessagesToClearArray = messagesToClearArrayList.toArray(new String[0]);
        logger.debug("newMessagesToClearArray: " + newMessagesToClearArray.toString());
        logger.debug("resulting JSON string: " + objectMapper.writeValueAsString(newMessagesToClearArray));

        user.setMessagesToClear(objectMapper.writeValueAsString(newMessagesToClearArray));
        repository.save(user);
    }

    public void resetMessagesToClear(final User user) {
        user.setMessagesToClear(null);
        repository.save(user);
    }

    public boolean toggleAutoclear(final User user) {
        user.setAutoclear(!user.isAutoclear());
        repository.save(user);
        return user.isAutoclear();
    }
}

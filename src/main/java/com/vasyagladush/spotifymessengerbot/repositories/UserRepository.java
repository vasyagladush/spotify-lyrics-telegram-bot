package com.vasyagladush.spotifymessengerbot.repositories;

import com.vasyagladush.spotifymessengerbot.models.User;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, UUID> {
    Optional<User> findFirstByMessengerPlatformAndMessengerUserIdAndMusicProviderPlatform(String messengerPlatform,
            String messengerUserId, String musicProviderPlatform);
}
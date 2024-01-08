package com.vasyagladush.spotifymessengerbot.models;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "messenger_user_id", "messenger_platform", "music_provider_platform" }) })
public class User {
    @Id
    @NonNull
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    private UUID id;

    @NonNull
    @Column(name = "messenger_user_id", nullable = false)
    private String messengerUserId;

    // @NonNull
    // @Enumerated(EnumType.STRING)
    // @Column(name = "messenger_platform", nullable = false, columnDefinition =
    // "messenger_platform_enum_type")
    // private MessengerPlatform messengerPlatform;

    // @Enumerated(EnumType.STRING)
    // @Column(name = "music_provider_platform", columnDefinition =
    // "music_provider_platform_enum_type")
    // // @Type(PostgreSQLEnumType.class)
    // private MusicProviderPlatform musicProviderPlatform;

    @NonNull
    @Column(name = "messenger_platform", nullable = false)
    private String messengerPlatform;

    @Column(name = "music_provider_platform")
    private String musicProviderPlatform;

    @Column(name = "music_provider_access_token")
    private String musicProviderAccessToken;

    @Column(name = "music_provider_refresh_token")
    private String musicProviderRefreshToken;

    @Column(name = "music_provider_access_token_expires_at")
    private Date musicProviderAccessTokenExpiresAt;

    @Column(name = "autoclear", nullable = false)
    private boolean autoclear;

    @Column(name = "messages_to_clear")
    private String messagesToClear;
}

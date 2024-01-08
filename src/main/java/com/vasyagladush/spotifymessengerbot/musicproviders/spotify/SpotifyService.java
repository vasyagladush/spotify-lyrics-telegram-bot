package com.vasyagladush.spotifymessengerbot.musicproviders.spotify;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.function.ThrowingFunction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vasyagladush.spotifymessengerbot.models.User;
import com.vasyagladush.spotifymessengerbot.musicproviders.spotify.types.SpotifyAccessTokenGrantedResponse;
import com.vasyagladush.spotifymessengerbot.musicproviders.spotify.types.SpotifyGetCurrentlyPlayingTrackResponse;
import com.vasyagladush.spotifymessengerbot.services.UserService;

// Might come in handy: https://www.baeldung.com/spring-inject-static-field

class ApiRequestFunctionParamsWithAccessToken<ParamsType> {
        String accessToken;
        ParamsType params;

        public ApiRequestFunctionParamsWithAccessToken(String accessToken, ParamsType params) {
                this.accessToken = accessToken;
                this.params = params;
        }
}

@Service
public class SpotifyService {
        private static final Logger logger = LogManager.getLogger(SpotifyService.class);
        private static final ObjectMapper objectMapper = new ObjectMapper();

        private final UserService userService;
        private final String apiKey;
        private final String apiSecret;
        private final String apiUrl;
        private final String oauthUrl;
        private final String authorizationCallbackUrl;
        private final String tokenUrl;

        private static final String scope = "user-read-currently-playing";
        private static final long TOKEN_EXPIRATION_OFFSET_MILLISECONDS = 60 * 1000;

        @Autowired
        public SpotifyService(@Value("${SPOTIFY_API_KEY}") String apiKey,
                        @Value("${SPOTIFY_API_SECRET}") String apiSecret,
                        @Value("${SPOTIFY_API_URL}") String apiUrl, @Value("${SPOTIFY_OAUTH_URL}") String oauthUrl,
                        @Value("${BASE_URL}") String baseUrl, @Value("${SPOTIFY_TOKEN_URL}") String tokenUrl,
                        UserService userService) {
                this.apiKey = apiKey;
                this.apiSecret = apiSecret;
                this.apiUrl = apiUrl;
                this.oauthUrl = oauthUrl;
                this.authorizationCallbackUrl = baseUrl + "callback/spotify/";
                this.tokenUrl = tokenUrl;
                this.userService = userService;

        }

        public String constructAuthorizationLink(final String state) {
                return String.format("%s?client_id=%s&response_type=code&state=%s&scope=%s&redirect_uri=%s",
                                this.oauthUrl,
                                this.apiKey, state,
                                URLEncoder.encode(SpotifyService.scope, StandardCharsets.UTF_8),
                                URLEncoder.encode(this.authorizationCallbackUrl, StandardCharsets.UTF_8));
        }

        public SpotifyAccessTokenGrantedResponse exchangeCodeOnAccessToken(final String code, final String state)
                        throws JsonProcessingException, IOException, ClientProtocolException {
                CloseableHttpClient httpClient = HttpClients.createDefault();

                HttpPost httpPost = new HttpPost(this.tokenUrl);

                String formUrlEncodedStringRequestBody = String.format(
                                "grant_type=authorization_code&code=%s&redirect_uri=%s",
                                code, this.authorizationCallbackUrl);

                logger.debug("Spotify grant access token request body: " + formUrlEncodedStringRequestBody);

                HttpEntity stringEntity = new StringEntity(formUrlEncodedStringRequestBody,
                                ContentType.APPLICATION_FORM_URLENCODED);

                httpPost.setEntity(stringEntity);
                httpPost.setHeader("Authorization", "Basic " + this.constructBasicClientCredentialsToken());

                final CloseableHttpResponse response = httpClient.execute(httpPost);

                final HttpEntity responseEntity = response.getEntity();

                logger.debug("Spotify grant access token response entity: " + responseEntity);

                String responseContent = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

                return objectMapper.readValue(responseContent,
                                SpotifyAccessTokenGrantedResponse.class);
        }

        public SpotifyAccessTokenGrantedResponse refreshAccessToken(String refreshToken)
                        throws JsonProcessingException, IOException, ClientProtocolException {
                CloseableHttpClient httpClient = HttpClients.createDefault();

                HttpPost httpPost = new HttpPost(this.tokenUrl);

                String formUrlEncodedStringRequestBody = String.format(
                                "grant_type=refresh_token&refresh_token=%s&client_id=%s",
                                refreshToken, this.apiKey);

                logger.debug("Spotify refresh access token request body: " + formUrlEncodedStringRequestBody);

                HttpEntity stringEntity = new StringEntity(formUrlEncodedStringRequestBody,
                                ContentType.APPLICATION_FORM_URLENCODED);

                httpPost.setEntity(stringEntity);
                httpPost.setHeader("Authorization", "Basic " + this.constructBasicClientCredentialsToken());

                final CloseableHttpResponse response = httpClient.execute(httpPost);

                final HttpEntity responseEntity = response.getEntity();

                logger.debug("Spotify refresh access token response entity: " + responseEntity);

                String responseContent = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

                return objectMapper.readValue(responseContent,
                                SpotifyAccessTokenGrantedResponse.class);
        }

        public SpotifyGetCurrentlyPlayingTrackResponse getCurrentlyPlayingTrack(User user)
                        throws JsonProcessingException, IOException, ClientProtocolException {
                return this.<Object, SpotifyGetCurrentlyPlayingTrackResponse>sendApiRequest(user, null,
                                this::_getCurrentlyPlayingTrack);
        }

        private SpotifyGetCurrentlyPlayingTrackResponse _getCurrentlyPlayingTrack(
                        ApiRequestFunctionParamsWithAccessToken<?> args)
                        throws JsonProcessingException, IOException, ClientProtocolException {
                CloseableHttpClient httpClient = HttpClients.createDefault();

                HttpGet httpGet = new HttpGet(this.apiUrl + "v1/me/player/currently-playing");

                httpGet.setHeader("Authorization", "Bearer " + args.accessToken);

                final CloseableHttpResponse response = httpClient.execute(httpGet);

                final HttpEntity responseEntity = response.getEntity();

                logger.debug("Spotify get currently playing track response entity: " + responseEntity);

                if (responseEntity == null)
                        return null;

                String responseContent = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);

                return objectMapper.readValue(responseContent,
                                SpotifyGetCurrentlyPlayingTrackResponse.class);
        }

        private String constructBasicClientCredentialsToken() {
                return Base64.encodeBase64String(String.format("%s:%s", this.apiKey, this.apiSecret).getBytes());
        }

        private <ApiRequestFunctionParamsType, ApiRequestFunctionReturnType> ApiRequestFunctionReturnType sendApiRequest(
                        User user,
                        ApiRequestFunctionParamsType apiRequestFunctionParams,
                        final ThrowingFunction<ApiRequestFunctionParamsWithAccessToken<ApiRequestFunctionParamsType>, ApiRequestFunctionReturnType> apiRequestFunction)
                        throws JsonProcessingException, IOException, ClientProtocolException {

                if (user.getMusicProviderAccessTokenExpiresAt().getTime()
                                - new Date().getTime() <= SpotifyService.TOKEN_EXPIRATION_OFFSET_MILLISECONDS) {
                        logger.debug("Spotify access token expired, starting refreshing, user id: " + user.getId());

                        SpotifyAccessTokenGrantedResponse refreshAccessTokenResponse = this
                                        .refreshAccessToken(user.getMusicProviderRefreshToken());

                        final Date now = new Date();
                        this.userService.updateWithMusicProviderAccessTokens(user,
                                        refreshAccessTokenResponse.access_token,
                                        refreshAccessTokenResponse.refresh_token,
                                        new Date(now.getTime() + refreshAccessTokenResponse.expires_in * 1000));

                        logger.debug("Spotify access token refreshed, user id: " + user.getId());
                }

                return apiRequestFunction
                                .apply(new ApiRequestFunctionParamsWithAccessToken<ApiRequestFunctionParamsType>(
                                                user.getMusicProviderAccessToken(), apiRequestFunctionParams));
        }
}

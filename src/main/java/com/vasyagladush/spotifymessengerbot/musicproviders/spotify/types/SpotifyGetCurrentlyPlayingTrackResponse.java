package com.vasyagladush.spotifymessengerbot.musicproviders.spotify.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotifyGetCurrentlyPlayingTrackResponse {

    @JsonProperty("item")
    private TrackItem item;

    // You may include other fields as needed

    public TrackItem getItem() {
        return item;
    }

    public void setItem(TrackItem item) {
        this.item = item;
    }

    // You may include getter and setter methods for other fields as needed

    // Inner class to represent the "item" field in the JSON response
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TrackItem {

        @JsonProperty("name")
        private String name;

        @JsonProperty("artists")
        private Artist[] artists;

        // You may include other fields as needed

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Artist[] getArtists() {
            return artists;
        }

        public void setArtists(Artist[] artists) {
            this.artists = artists;
        }

        // You may include getter and setter methods for other fields as needed
    }

    // Inner class to represent the "artists" field in the JSON response
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Artist {

        @JsonProperty("name")
        private String name;

        // You may include other fields as needed

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        // You may include getter and setter methods for other fields as needed
    }
}
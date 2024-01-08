package com.vasyagladush.spotifymessengerbot.lyricsproviders.genius;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import core.GLA;

// Almost the same as  com.github.LowLevelSubmarine.core.GLA
// public class GeniusService {
//     // private GeniusHttpManager httpManager = new GeniusHttpManager();

//     // public SongSearch search(String query) throws IOException {
//     //     return new SongSearch(this, query);
//     // }

//     // public GeniusHttpManager getHttpManager() {
//     //     return this.httpManager;
//     // }
// }

@Service
public class GeniusService {
    private static final Logger logger = LogManager.getLogger(GeniusService.class);
    private static GLA gla = new GLA();

    public String getSongLyrics(final String songName, final String artistsNames) throws IOException {
        logger.debug("Genius lyrics search starts");
        return GeniusService.gla.search(songName + " " + artistsNames).getHits().get(0).fetchLyrics();
    }
}
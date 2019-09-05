package com.muva.bamburi.utils.audio_player;

/**
 * Created by Njoro on 6/1/18.
 */
public interface PlayerAdapter {
    void loadMedia(String resourcePath);

    void release();

    boolean isPlaying();

    void play();

    void reset();

    void pause();

    void initializeProgressCallback();

    void seekTo(int position);
}

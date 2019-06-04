package com.example.videolibrary;

public interface VideoStateListener {
    void onStart();

    void onPause();

    void onPauseToStart();

    void onStop();

    void onRePlay();
}

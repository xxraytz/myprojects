package com.mobimore.graphicsObjects;

public interface Controllable {
    void pause();
    void resume();
    void restart();
    void stepBack();
    void stepFW();
    int getCurrentFrameNumber();
    boolean isPaused();
}

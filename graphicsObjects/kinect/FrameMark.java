package com.mobimore.graphicsObjects.kinect;

public class FrameMark<T extends Number>{
    int frameNumber;
    T mark;

    public FrameMark(int frameNumber, T mark) {
        this.frameNumber = frameNumber;
        this.mark = mark;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }

    public T getMark() {
        return mark;
    }

    public void setMark(T mark) {
        this.mark = mark;
    }
}
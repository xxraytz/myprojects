package com.mobimore.GTest;

import com.mobimore.graphicsObjects.kinect.SkeletonDrawable;

public interface SkeletonDrawableObserver {
    void onSceneEnded(SkeletonDrawable sender);
    void onSceneStarted(SkeletonDrawable sender);
}

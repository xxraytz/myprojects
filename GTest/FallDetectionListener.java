package com.mobimore.GTest;

import com.mobimore.utils.LibSVM;

public interface FallDetectionListener {
    void onFallDetected(int frameNumber);
    void onFallUndetected(int frameNumber);
    void onNewFrameProcessed(boolean expertVal, LibSVM.SVMResult svmResult, boolean resultVal, int frameNumber);
}

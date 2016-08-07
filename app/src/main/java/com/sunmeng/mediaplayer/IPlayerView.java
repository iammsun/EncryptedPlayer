package com.sunmeng.mediaplayer;

import android.support.annotation.IntRange;
import android.support.annotation.MainThread;

/**
 * Created by sunmeng on 16/8/6.
 */
@MainThread
public interface IPlayerView {

    void onMediaReady(@IntRange(from = 0) long duration);

    void onMediaStart();

    void onMediaPause();

    void onMediaComplete();

    void onResize(int videoWidth, int videoHeight);

    void onMediaError();
}

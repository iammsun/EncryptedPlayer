package com.sunmeng.mediaplayer.downloader;

import android.support.annotation.IntRange;
import android.support.annotation.MainThread;

/**
 * Created by sunmeng on 16/8/8.
 */
@MainThread
public interface ProgressListener {

    void onStart();

    void onFinish();

    void onError(Throwable throwable);

    void onProgress(@IntRange(from = 0, to = 100) int progress);
}
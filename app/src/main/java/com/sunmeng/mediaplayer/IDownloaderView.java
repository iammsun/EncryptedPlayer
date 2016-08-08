package com.sunmeng.mediaplayer;

import android.support.annotation.IntRange;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

/**
 * Created by sunmeng on 16/8/6.
 */
@MainThread
public interface IDownloaderView {

    void onDownloadStart();

    void onDownloadError();

    void onDownloadProgress(@IntRange(from = 0, to = 100) int progress);

    void onDownloadComplete(@NonNull String path);
}

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

    void onDownloadProgress(@IntRange(from = DownloadTask.ProgressListener.START, to =
            DownloadTask.ProgressListener.FINISH) int progress);

    void onDownloadError();

    void onDownloadComplete(@NonNull String path);
}

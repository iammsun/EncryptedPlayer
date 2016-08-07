package com.sunmeng.mediaplayer;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

/**
 * Created by sunmeng on 16/8/6.
 */
public class DownloadPresenter implements DownloadTask.ProgressListener {

    private IDownloaderView mDownloaderView;
    private Context mContext;
    private String mCacheFilePath;

    public DownloadPresenter(@NonNull Context context, @NonNull IDownloaderView downloaderView) {
        mContext = context;
        mDownloaderView = downloaderView;
    }

    public void download(Uri uri) {
        DownloadTask downloadTask = new DownloadTask(new EncryptImpl());
        downloadTask.setProgressListener(this);
        mCacheFilePath = Utils.generateCacheFilePath(mContext, uri.toString().substring(uri
                .toString().lastIndexOf(".") + 1));
        downloadTask.execute(uri.toString(), mCacheFilePath);
    }

    @Override
    public void onProgress(@IntRange(from = ERROR, to = FINISH) int progress) {
        switch (progress) {
            case DownloadTask.ProgressListener.ERROR:
                mDownloaderView.onDownloadError();
                break;
            case DownloadTask.ProgressListener.FINISH:
                mDownloaderView.onDownloadComplete(mCacheFilePath);
                break;
            case DownloadTask.ProgressListener.START:
                mDownloaderView.onDownloadStart();
                break;
            default:
                mDownloaderView.onDownloadProgress(progress);
        }
    }
}

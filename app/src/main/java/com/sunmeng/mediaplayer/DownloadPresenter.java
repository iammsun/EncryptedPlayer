package com.sunmeng.mediaplayer;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

import com.sunmeng.mediaplayer.downloader.DownloadManager;
import com.sunmeng.mediaplayer.downloader.DownloadTask;
import com.sunmeng.mediaplayer.downloader.ProgressListener;

/**
 * Created by sunmeng on 16/8/6.
 */
public class DownloadPresenter implements BasePresenter<IDownloaderView>, ProgressListener {

    private IDownloaderView mDownloaderView;
    private Context mContext;
    private String mCacheFilePath;
    private DownloadManager mDownloadManager = DownloadManager.getInstance();
    private DownloadTask mTask;

    public DownloadPresenter(@NonNull Context context) {
        mContext = context;
    }

    public void download(Uri uri) {
        if (mTask != null) {
            mTask.cancel(true);
        }
        mCacheFilePath = Utils.generateCacheFilePath(mContext, uri.toString().substring(uri
                .toString().lastIndexOf(".") + 1));
        mTask = mDownloadManager.download(uri.toString(), mCacheFilePath, this);
    }

    @Override
    public void onStart() {
        mDownloaderView.onDownloadStart();
    }

    @Override
    public void onFinish() {
        mTask = null;
        mDownloaderView.onDownloadComplete(mCacheFilePath);
    }

    @Override
    public void onError(Throwable throwable) {
        mTask = null;
        mDownloaderView.onDownloadError();
    }

    @Override
    public void onProgress(@IntRange(from = 0, to = 100) int progress) {
        mDownloaderView.onDownloadProgress(progress);
    }

    @Override
    public void attachView(@NonNull IDownloaderView view) {
        mDownloaderView = view;
    }

    @Override
    public void detachView() {
        if (mTask != null) {
            mTask.cancel(true);
        }
    }
}

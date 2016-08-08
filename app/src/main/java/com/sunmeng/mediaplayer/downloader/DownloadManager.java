package com.sunmeng.mediaplayer.downloader;

/**
 * Created by sunmeng on 16/8/8.
 */
public class DownloadManager {

    private IEncrypt mEncrypt;

    private DownloadManager(IEncrypt mEncrypt) {
        this.mEncrypt = mEncrypt;
    }

    private static DownloadManager instance;

    public static void init(IEncrypt encrypt) {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager(encrypt);
                }
            }
        }
    }

    public static DownloadManager getInstance() {
        return instance;
    }

    public DownloadTask download(String httpUrl, String targetPath, ProgressListener listener) {
        DownloadTask task = new DownloadTask(mEncrypt);
        task.setProgressListener(listener);
        task.execute(httpUrl, targetPath);
        return task;
    }
}

package com.sunmeng.mediaplayer;

import android.app.Application;

import com.sunmeng.mediaplayer.downloader.DownloadManager;
import com.sunmeng.mediaplayer.downloader.IEncrypt;

/**
 * Created by sunmeng on 16/8/8.
 */
public class BaseApp extends Application {

    private IEncrypt encrypt = new EncryptImpl();

    @Override
    public void onCreate() {
        super.onCreate();
        DownloadManager.init(encrypt);
        MediaServer.init(encrypt);
        if (!MediaServer.getInstance().isAlive()) {
            MediaServer.getInstance().start();
        }
    }
}

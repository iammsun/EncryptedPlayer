package com.sunmeng.mediaplayer.downloader;

import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by sunmeng on 16/8/8.
 */
public class DownloadTask extends AsyncTask<String, Integer, Boolean> {

    private static final int BUFFER_SIZE = 10 * 1024 * 1024;

    private ProgressListener mProgressListener;
    private int mProgress = -1;
    private final IEncrypt encrypt;

    public DownloadTask(IEncrypt encrypt) {
        this.encrypt = encrypt;
    }

    public void setProgressListener(ProgressListener progressListener) {
        mProgressListener = progressListener;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (params == null || params.length != 2) {
            return false;
        }
        String httpUrl = params[0];
        String filePath = params[1];
        InputStream is = null;
        FileOutputStream os = null;
        File target = new File(filePath);
        if (target.exists() && !target.delete()) {
            return false;
        }
        if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
            return false;
        }
        try {
            if (!target.createNewFile()) {
                return false;
            }
            URL url = new URL(httpUrl);
            URLConnection con = url.openConnection();
            is = con.getInputStream();
            os = new FileOutputStream(target);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bufLen = 0;
            int readLen = 0;
            int contentLen = con.getContentLength();
            if (contentLen > 0) {
                readLen += bufLen;
                publishProgress(readLen, contentLen);
            }
            if (encrypt != null) {
                os.write(encrypt.getSignature().getBytes());
            }
            while ((bufLen = is.read(buffer)) != -1) {
                if (encrypt != null) {
                    buffer = encrypt.encrypt(buffer, 0, bufLen);
                }
                os.write(buffer, 0, bufLen);
                if (contentLen > 0) {
                    readLen += bufLen;
                    publishProgress(readLen, contentLen);
                }
            }
            os.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(os);
            IOUtils.close(is);
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mProgressListener == null) {
            return;
        }
        if (result) {
            mProgressListener.onFinish();
        } else {
            mProgressListener.onError(new RuntimeException("failed to download"));
        }
    }

    @Override
    protected void onPreExecute() {
        if (mProgressListener == null) {
            return;
        }
        mProgressListener.onStart();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (mProgressListener == null) {
            return;
        }
        int read = values[0];
        int contentLen = values[1];
        if (contentLen <= 0) {
            return;
        }
        int current = (int) ((float) read / contentLen * 100);
        if (current == mProgress) {
            return;
        }
        mProgress = current;
        if (mProgress >= 0) {
            mProgressListener.onProgress(mProgress);
        }
    }
}

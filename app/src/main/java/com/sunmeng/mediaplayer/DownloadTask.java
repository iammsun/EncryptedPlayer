package com.sunmeng.mediaplayer;

import android.os.AsyncTask;
import android.support.annotation.IntRange;
import android.support.annotation.MainThread;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by sunmeng on 16/8/6.
 */
public class DownloadTask extends AsyncTask<String, Integer, Boolean> {

    public interface ProgressListener {

        int ERROR = -2;
        int UNKNOWN = -1;
        int START = 0;
        int FINISH = 100;

        @MainThread
        void onProgress(@IntRange(from = ERROR, to = FINISH) int progress);
    }

    private ProgressListener mProgressListener;
    private int mProgress;
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
            byte[] buffer = new byte[4 * 1024];
            int bufLen;
            int readLen = 0;
            int contentLen = con.getContentLength();
            if (encrypt != null) {
                os.write(encrypt.getSignature().getBytes());
            }
            while ((bufLen = is.read(buffer)) != -1) {
                if (encrypt != null) {
                    buffer = encrypt.encrypt(buffer, 0, bufLen);
                }
                os.write(buffer, 0, bufLen);
                readLen += bufLen;
                publishProgress(readLen, contentLen);
            }
            os.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Utils.close(os);
            Utils.close(is);
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mProgressListener == null) {
            return;
        }
        mProgress = result ? ProgressListener.FINISH : ProgressListener.ERROR;
        mProgressListener.onProgress(mProgress);
    }

    @Override
    protected void onPreExecute() {
        if (mProgressListener == null) {
            return;
        }
        mProgress = ProgressListener.START;
        mProgressListener.onProgress(mProgress);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        if (mProgressListener == null) {
            return;
        }
        int read = values[0];
        int contentLen = values[1];
        int current = contentLen == -1 ? ProgressListener.UNKNOWN : (int) ((float) read /
                contentLen * ProgressListener.FINISH);
        if (current == mProgress) {
            return;
        }
        mProgress = current;
        mProgressListener.onProgress(mProgress);
    }
}

package com.sunmeng.mediaplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.LocalSocket;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * Created by sunmeng on 16/8/6.
 */
public class MediaPlayPresenter implements BasePresenter<IPlayerView> {

    private static final String TAG = "MediaPlayPresenter";

    private IPlayerView mPlayerView;
    private Context mContext;
    private MediaPlayer mPlayer;

    public MediaPlayPresenter(@NonNull Context context) {
        mContext = context;
    }

    public void initPlayer(@NonNull SurfaceView surfaceView, @NonNull String path) {
        close();
        MediaPlayer player = null;
        try {
            player = new MediaPlayer();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mPlayer = mp;
                    mPlayerView.onMediaReady(mPlayer.getDuration());
                    mPlayerView.onResize(mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
                    playOrPause();
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.d(TAG, String.format("media error, what: %d, extra: %d", what, extra));
                    mPlayerView.onMediaError();
                    return true;
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mPlayerView.onMediaComplete();
                }
            });
            player.reset();
            player.setDataSource(mContext, Uri.parse("http://localhost:" + MediaServer
                    .getInstance().getPort() + "/" + path));
            player.setDisplay(surfaceView.getHolder());
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setScreenOnWhilePlaying(true);
            player.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            if (player != null) {
                player.reset();
                player.release();
            }
            mPlayerView.onMediaError();
        }
    }

    public void playOrPause() {
        if (!isReady()) {
            return;
        }
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            mPlayerView.onMediaPause();
        } else {
            mPlayer.start();
            mPlayerView.onMediaStart();
        }
    }

    public int getProgress() {
        if (!isReady()) {
            return 0;
        }
        return mPlayer.getCurrentPosition();
    }

    public void seek(int progress) {
        if (!isReady()) {
            return;
        }
        mPlayer.seekTo(progress);
    }

    public boolean isPlaying() {
        if (!isReady()) {
            return false;
        }
        return mPlayer.isPlaying();
    }

    public void close() {
        if (mPlayer == null) {
            return;
        }
        mPlayer.release();
        mPlayer = null;
    }

    public boolean isReady() {
        return mPlayer != null;
    }

    @Override
    public void attachView(IPlayerView view) {
        mPlayerView = view;
    }

    @Override
    public void detachView() {
        close();
    }
}

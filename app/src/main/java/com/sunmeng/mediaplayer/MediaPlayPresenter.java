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
public class MediaPlayPresenter {

    private static final String TAG = "MediaPlayPresenter";

    private IPlayerView mPlayerView;
    private Context mContext;
    private MediaPlayer mPlayer;
    private MediaServer mediaServer = MediaServer.getInstance(new EncryptImpl());

    public MediaPlayPresenter(@NonNull Context context, @NonNull IPlayerView playerView) {
        mContext = context;
        mPlayerView = playerView;
    }

    public void initPlayer(@NonNull SurfaceView surfaceView, @NonNull String path) {
        close();
        if (!mediaServer.isAlive()) {
            mediaServer.start();
        }
        MediaPlayer player = null;
        try {
            player = new MediaPlayer();
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
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setDataSource(mContext, Uri.parse("http://localhost:" + mediaServer.getPort() +
                    "/" + path));
            player.setDisplay(surfaceView.getHolder());
            player.setScreenOnWhilePlaying(true);
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mPlayer = mp;
                    mPlayerView.onMediaReady(mPlayer.getDuration());
                    mPlayerView.onResize(mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
                    playOrPause();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (player != null) {
                player.setDisplay(null);
                player.reset();
                player.release();
                player = null;
            }
            mPlayerView.onMediaError();
        }
        if (player == null) {
            return;
        }
        try {
            player.prepareAsync();
        }catch (IllegalStateException e){
            e.printStackTrace();
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
        mPlayer.setDisplay(null);
        mPlayer.reset();
        mPlayer.release();
        mPlayer = null;
    }

    public boolean isReady() {
        return mPlayer != null;
    }
}

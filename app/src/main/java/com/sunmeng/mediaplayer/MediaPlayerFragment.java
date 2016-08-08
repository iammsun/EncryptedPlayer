package com.sunmeng.mediaplayer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class MediaPlayerFragment extends Fragment
        implements SurfaceHolder.Callback, View.OnClickListener, IPlayerView, IDownloaderView {

    private static final String EXTRA_URL = "url";
    private static final String EXTRA_PATH = "path";

    private static final int RC_PERMISSION_DOWNLOAD = 1;
    private static final int RC_PERMISSION_PLAYER = 2;

    private static final String[] PERMISSIONS = new String[]{Manifest.permission
            .WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest
            .permission.INTERNET};

    private SurfaceView mSurfaceView;
    private boolean mHolderReady;

    private Uri mUri;
    private String mPath;

    private ImageView mPlayBtn;
    private ImageView mPreBtn;
    private ImageView mNextBtn;
    private SeekBar mProgress;
    private TextView mProgressTime;
    private TextView mDurationTime;
    private View mControlPanel;
    private ProgressDialog mProgressDialog;

    private MediaPlayPresenter mPresenter;
    private DownloadPresenter mDownloadPresenter;

    private Handler mUIHandler = new Handler();

    private Runnable progressJob = new Runnable() {
        @Override
        public void run() {
            mProgress.setProgress(mPresenter.getProgress());
            mProgressTime.setText(Utils.formatTime(mPresenter.getProgress()));
            mUIHandler.postDelayed(this, 1000);
        }
    };

    private Runnable hideControlPanelJob = new Runnable() {
        @Override
        public void run() {
            if (mControlPanel.getVisibility() != View.VISIBLE) {
                return;
            }
            mControlPanel.setVisibility(View.GONE);
        }
    };

    public static MediaPlayerFragment getInstance(@NonNull Uri uri) {
        MediaPlayerFragment fragment = new MediaPlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_URL, uri);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static MediaPlayerFragment getInstance(@NonNull String path) {
        MediaPlayerFragment fragment = new MediaPlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_PATH, path);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUri = getArguments().getParcelable(EXTRA_URL);
        mPath = getArguments().getString(EXTRA_PATH);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSurfaceView = (SurfaceView) view.findViewById(R.id.content_holder);
        mPlayBtn = (ImageView) view.findViewById(R.id.start_btn);
        mPreBtn = (ImageView) view.findViewById(R.id.prev_btn);
        mNextBtn = (ImageView) view.findViewById(R.id.next_btn);
        mDurationTime = (TextView) view.findViewById(R.id.duration_time);
        mProgressTime = (TextView) view.findViewById(R.id.progress_time);
        mProgress = (SeekBar) view.findViewById(R.id.progress);
        mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                if (mPresenter == null || !mPresenter.isReady()) {
                    mProgress.setProgress(0);
                    mProgressTime.setText(Utils.formatTime(0));
                    return;
                }
                mPresenter.seek(progress);
                mProgressTime.setText(Utils.formatTime(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mControlPanel = view.findViewById(R.id.control_pannel);
        mPresenter = new MediaPlayPresenter(getContext());
        mPresenter.attachView(this);
        mDownloadPresenter = new DownloadPresenter(getContext());
        mDownloadPresenter.attachView(this);

        mPlayBtn.setOnClickListener(this);
        mNextBtn.setOnClickListener(this);
        mPreBtn.setOnClickListener(this);
        mSurfaceView.setOnClickListener(this);

        mSurfaceView.getHolder().addCallback(this);
        if (mUri != null) {
            mDownloadPresenter.download(mUri);
        }
    }

    private void hideProgress() {
        if (mProgressDialog == null) {
            return;
        }
        mProgressDialog.dismiss();
        mProgressDialog = null;
    }

    private void showProgress(int style) {
        hideProgress();
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setProgressStyle(style);
        mProgressDialog.setMax(100);
        mProgressDialog.setTitle(R.string.download_title);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!mHolderReady) {
            mHolderReady = true;
            initPlayer();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mHolderReady = false;
        mPresenter.close();
    }

    private void initPlayer() {
        if (!mHolderReady) {
            return;
        }
        if (TextUtils.isEmpty(mPath)) {
            return;
        }
        mPresenter.initPlayer(mSurfaceView, mPath);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_btn:
                mPresenter.playOrPause();
                break;
            case R.id.next_btn:
                onNextClick();
                break;
            case R.id.prev_btn:
                onPrevClick();
                break;
            case R.id.content_holder:
                onContentClick();
                break;
        }
    }

    private void onNextClick() {
    }

    private void onPrevClick() {

    }

    private void onContentClick() {
        if (mControlPanel.getVisibility() == View.VISIBLE) {
            if (!mPresenter.isPlaying()) {
                return;
            }
            hideControlPanel(false);
        } else {
            showControlPanel();
            hideControlPanel(true);
        }
    }

    private void showControlPanel() {
        mUIHandler.removeCallbacks(hideControlPanelJob);
        mControlPanel.setVisibility(View.VISIBLE);
    }

    private void hideControlPanel(boolean lazy) {
        mUIHandler.removeCallbacks(hideControlPanelJob);
        if (lazy) {
            mUIHandler.postDelayed(hideControlPanelJob, 3000);
        } else {
            mUIHandler.post(hideControlPanelJob);
        }
    }

    @Override
    public void onMediaReady(long duration) {
        mProgress.setMax((int) duration);
        mProgress.setProgress(0);
        mDurationTime.setText(Utils.formatTime(duration));
        mProgressTime.setText(Utils.formatTime(0));
        showControlPanel();
    }

    @Override
    public void onMediaStart() {
        mPlayBtn.setImageResource(android.R.drawable.ic_media_pause);
        mUIHandler.postDelayed(progressJob, 1000);
        hideControlPanel(true);
    }

    @Override
    public void onMediaPause() {
        mPlayBtn.setImageResource(android.R.drawable.ic_media_play);
        mUIHandler.removeCallbacks(progressJob);
        showControlPanel();
    }

    @Override
    public void onMediaComplete() {
        mPlayBtn.setImageResource(android.R.drawable.ic_media_play);
        mProgress.setProgress(0);
        mProgressTime.setText(Utils.formatTime(0));
        mUIHandler.removeCallbacks(progressJob);
        showControlPanel();
    }

    @Override
    public void onResize(int videoWidth, int videoHeight) {
    }

    @Override
    public void onMediaError() {
        mPlayBtn.setImageResource(android.R.drawable.ic_media_play);
        mProgress.setProgress(0);
        mProgressTime.setText(Utils.formatTime(0));
        mUIHandler.removeCallbacks(progressJob);
        if (!checkPermission(RC_PERMISSION_PLAYER)) {
            return;
        }
        showErrorDialog(R.string.decode_error);
    }

    private void showErrorDialog(int resId) {
        new AlertDialog.Builder(getContext()).setMessage(resId)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                }).setCancelable(false).create().show();
    }

    private boolean checkPermission(int requestCode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (Utils.permissionCheck(getContext(), PERMISSIONS)) {
            return true;
        }
        requestPermissions(PERMISSIONS, requestCode);
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (!Utils.permissionCheck(getContext(), PERMISSIONS)) {
            getActivity().finish();
        }
        switch (requestCode) {
            case RC_PERMISSION_PLAYER:
                initPlayer();
                break;
            case RC_PERMISSION_DOWNLOAD:
                mDownloadPresenter.download(mUri);
        }
    }

    @Override
    public void onDownloadProgress(@IntRange(from = 0, to = 100) int progress) {
        if (mProgressDialog == null || !mProgressDialog.isShowing() || progress == 0) {
            showProgress(ProgressDialog.STYLE_HORIZONTAL);
        }
        mProgressDialog.setProgress(progress);
    }

    @Override
    public void onDownloadStart() {
        showProgress(ProgressDialog.STYLE_SPINNER);
    }

    @Override
    public void onDownloadError() {
        hideProgress();
        if (!checkPermission(RC_PERMISSION_DOWNLOAD)) {
            return;
        }
        showErrorDialog(R.string.download_error);
    }

    @Override
    public void onDownloadComplete(@NonNull String path) {
        hideProgress();
        mPath = path;
        Toast.makeText(getContext(), mPath, Toast.LENGTH_SHORT).show();
        initPlayer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
        mDownloadPresenter.detachView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUIHandler.removeCallbacksAndMessages(null);
        mPresenter.close();
    }
}

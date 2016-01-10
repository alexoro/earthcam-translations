package com.uas.translations.player;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.uas.translations.R;
import com.uas.translations.models.CameraInfo;
import com.uas.translations.utils.ActivityUtils;
import com.uas.videoplayer.interfaces.IVideoPlayer;
import com.uas.videoplayer.interfaces.VideoPlayerException;
import com.uas.videoplayer.mediaplayerimpl.VideoPlayerFragment;

/**
 * Created by UAS on 09.01.2016.
 */
public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_CAMERA_INFO = "camera_info";

    public static Intent createLaunchIntent(Context context, CameraInfo cameraInfo) {
        Intent intent = new Intent(context, PlayerActivity.class);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EXTRA_CAMERA_INFO, cameraInfo);
        return intent;
    }


    private static final String[] RATIO_NAMES = new String[] {
            "Auto",
            "4:3",
            "16:9",
            "16:10"
    };
    private static final IVideoPlayer.AspectRatio[] RATIO_VALUES = new IVideoPlayer.AspectRatio[] {
            IVideoPlayer.AspectRatio.MATCH_CONTAINER,
            IVideoPlayer.AspectRatio.ASPECT_4_3,
            IVideoPlayer.AspectRatio.ASPECT_16_9,
            IVideoPlayer.AspectRatio.ASPECT_16_10
    };
    private static final long TOOLBAR_HIDE_DELAY = 2500L;


    private CameraInfo mCameraInfo;

    private Toolbar mToolbar;
    private FrameLayout mPlayerContainer;
    private ProgressBar mProgress;
    private Spinner mSpinner;

    private IVideoPlayer mVideoPlayer;
    private OnWindowStateChangedListenerImpl mOnWindowStateChangedListener;
    private OnPlayerStateChangedListenerImpl mOnPlayerStateChangedListener;
    private Dialog mDialog;
    private Handler mHandler;
    private int mSystemUiDefaultFlags;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtils.removeActivityBackgroundCompat(this);
        setContentView(R.layout.activity_player);

        mCameraInfo = (CameraInfo) getIntent().getSerializableExtra(EXTRA_CAMERA_INFO);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mPlayerContainer = (FrameLayout) findViewById(R.id.player);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mSpinner = (Spinner) findViewById(R.id.spinner);
        mSystemUiDefaultFlags = getWindow().getDecorView().getSystemUiVisibility();

        final VideoPlayerFragment playerFragment = VideoPlayerFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(mPlayerContainer.getId(), playerFragment, null)
                .commit();
        mVideoPlayer = playerFragment;

        mToolbar.setTitle(mCameraInfo.getName());
        mToolbar.setLogo(null);
        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_item, RATIO_NAMES);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setSelection(0, false);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view,
                                       int position,
                                       long id) {
                onRatioChanged(RATIO_VALUES[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mPlayerContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoPlayer.getCurrentPlayerState() == IVideoPlayer.PlayerState.STARTED) {
                    mHandler.removeCallbacksAndMessages(null);
                    if (mToolbar.getVisibility() == View.VISIBLE) {
                        hideToolbar();
                    } else {
                        showToolbar();
                        scheduleToolbarHide();
                    }
                }
            }
        });

        mOnWindowStateChangedListener = new OnWindowStateChangedListenerImpl();
        mOnPlayerStateChangedListener = new OnPlayerStateChangedListenerImpl();

        mOnWindowStateChangedListener.onWindowStateChanged(mVideoPlayer, IVideoPlayer.WindowState.NOT_INITIALIZED);
        mOnPlayerStateChangedListener.onPlayerStateChanged(mVideoPlayer, IVideoPlayer.PlayerState.STOPPED);

        playerFragment.registerOnWindowStateChangedListener(mOnWindowStateChangedListener);
        playerFragment.registerOnPlayerStateChangedListener(mOnPlayerStateChangedListener);

        Uri address = Uri.parse(mCameraInfo.getLink());
        mVideoPlayer.changeStreamAddress(address, true);

        mHandler = new Handler();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
        mVideoPlayer.unregisterOnWindowStateChangedListener(mOnWindowStateChangedListener);
        mVideoPlayer.unregisterOnPlayerStateChangedListener(mOnPlayerStateChangedListener);
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    protected void onRatioChanged(IVideoPlayer.AspectRatio aspectRatio) {
        mVideoPlayer.setAspectRatio(aspectRatio);
    }

    protected void showToolbar() {
        mToolbar.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= 16) {
            getWindow().getDecorView().setSystemUiVisibility(mSystemUiDefaultFlags);
        }
    }

    protected void hideToolbar() {
        mToolbar.setVisibility(View.GONE);
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        if (Build.VERSION.SDK_INT >= 16) {
            flags = flags
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        }
        if (Build.VERSION.SDK_INT >= 19) {
            flags = flags
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    protected void scheduleToolbarHide() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hideToolbar();
            }
        }, TOOLBAR_HIDE_DELAY);
    }


    private class OnWindowStateChangedListenerImpl implements IVideoPlayer.OnWindowStateChangedListener {
        @Override
        public void onWindowStateChanged(IVideoPlayer player, IVideoPlayer.WindowState windowState) {
            switch (windowState) {
                case NOT_INITIALIZED:
                    mProgress.setVisibility(View.GONE);
                    break;
                case INITIALIZING:
                    mProgress.setVisibility(View.VISIBLE);
                    break;
                case READY:
                    mProgress.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private class OnPlayerStateChangedListenerImpl implements IVideoPlayer.OnPlayerStateChangedListener {

        @Override
        public void onPlayerStateChanged(IVideoPlayer player, IVideoPlayer.PlayerState playerState) {
            switch (playerState) {
                case STARTED:
                    scheduleToolbarHide();
                    break;
                case IDLE:
                case STOPPED:
                case PAUSED:
                case PLAYBACK_COMPLETED:
                case ERROR:
                    showToolbar();
                    break;
            }
        }

        @Override
        public void onBufferingStateChanged(IVideoPlayer player, IVideoPlayer.BufferingState bufferingState) {
            switch (bufferingState) {
                case BEGIN:
                    mProgress.setVisibility(View.VISIBLE);
                    break;
                case END:
                case CANCELLED:
                    mProgress.setVisibility(View.GONE);
                    break;
            }
        }

        @Override
        public void onVideoInfoReceived(IVideoPlayer player, boolean isStreaming, int duration) {

        }

        @Override
        public void onPlayPositionChanged(IVideoPlayer player, int playPositionInMillis, int durationInMillis) {

        }

        @Override
        public void onPlayPositionChangeRequested(IVideoPlayer player, int newPlayPositionInMillis, int durationInMillis) {

        }

        @Override
        public void onVolumeChanged(IVideoPlayer videoPlayer, int volumeCurrentLevel, int volumeMaxLevel) {

        }

        @Override
        public void onAspectRatioChanged(IVideoPlayer videoPlayer, IVideoPlayer.AspectRatio aspectRatio) {

        }

        @Override
        public void onError(IVideoPlayer player, VideoPlayerException ex) {
            mDialog = new AlertDialog.Builder(PlayerActivity.this)
                    .setTitle("Error!")
                    .setMessage(ex.toString())
                    .setCancelable(false)
                    .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mVideoPlayer.play();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        }
    }

}
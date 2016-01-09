package com.uas.videoplayer.mediaplayerimpl;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.uas.videoplayer.interfaces.IVideoPlayer;
import com.uas.videoplayer.interfaces.VideoPlayerException;

/**
 * Created by a.sorokin@vectordigital.ru on 03.02.2015.
 *
 * @author a.sorokin@vectordigital.ru
 */
public class VideoPlayerTestActivity extends FragmentActivity {

    private static final String LOG_TAG = "MediaPlayer/Host";

    private Button vStream1;
    private Button vStream2;
    private Button vForceError;
    private ViewGroup vControls;
    private Button vPlayPause;
    private TextView vPosition;
    private SeekBar vSeekBar;
    private TextView vDuration;
    private Button vAspectAuto;
    private Button vAspect43;
    private Button vAspect169;
    private Button vAspect1610;
    private ProgressBar vProgress;

    private IVideoPlayer mVideoPlayer;

    private OnWindowStateChangedListenerImpl mOnWindowStateChangedListener;
    private OnPlayerStateChangedListenerImpl mOnPlayerStateChangedListener;

    private Dialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player_test_activity);

        final VideoPlayerFragment playerFragment = VideoPlayerFragment.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.player, playerFragment, null)
                .commitAllowingStateLoss();
        mVideoPlayer = playerFragment;

        vStream1 = (Button) findViewById(R.id.stream1);
        vStream2 = (Button) findViewById(R.id.stream2);
        vForceError = (Button) findViewById(R.id.force_error);
        vControls = (ViewGroup) findViewById(R.id.controls);
        vPlayPause = (Button) findViewById(R.id.play_pause);
        vPosition = (TextView) findViewById(R.id.position);
        vSeekBar = (SeekBar) findViewById(R.id.seekbar);
        vDuration = (TextView) findViewById(R.id.duration);
        vAspectAuto = (Button) findViewById(R.id.aspect_auto);
        vAspect43 = (Button) findViewById(R.id.aspect_4_3);
        vAspect169 = (Button) findViewById(R.id.aspect_16_9);
        vAspect1610 = (Button) findViewById(R.id.aspect_16_10);
        vProgress = (ProgressBar) findViewById(R.id.progress);

        final Uri streamAddress1 = Uri.parse("http://video2.earthcam.com:1935/fecnetwork/4098.flv/playlist.m3u8");
        final Uri streamAddress2 = Uri.parse("http://play.vs.nemo.tv/hls-adapt/569/play.m3u8?nodrm=1&gmts=1423818600&stream_pool_id=1499984&area=6992&gmte=1423821600&uid=842236");

        mOnWindowStateChangedListener = new OnWindowStateChangedListenerImpl();
        mOnPlayerStateChangedListener = new OnPlayerStateChangedListenerImpl();

        mOnWindowStateChangedListener.onWindowStateChanged(mVideoPlayer, IVideoPlayer.WindowState.NOT_INITIALIZED);
        mOnPlayerStateChangedListener.onPlayerStateChanged(mVideoPlayer, IVideoPlayer.PlayerState.STOPPED);

        playerFragment.registerOnWindowStateChangedListener(mOnWindowStateChangedListener);
        playerFragment.registerOnPlayerStateChangedListener(mOnPlayerStateChangedListener);

        vStream1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoPlayer.changeStreamAddress(streamAddress1, true);

                /*IVideoPlayer14.PlayerSnapShot snapShot = new IVideoPlayer14.PlayerSnapShot();
                snapShot.streamAddress = streamAddress1;
                snapShot.targetState = IVideoPlayer14.PlayerState.STARTED;
                snapShot.playPosition = 0;
                snapShot.duration = IVideoPlayer14.DURATION_FOR_LIVE;
                snapShot.error = null;
                snapShot.forceIsStreaming = true;
                snapShot.aspectRatio = IVideoPlayer14.AspectRatio.MATCH_CONTAINER;
                mVideoPlayer.restore(snapShot);*/
            }
        });
        vStream2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoPlayer.changeStreamAddress(streamAddress2, false);

                /*IVideoPlayer14.PlayerSnapShot snapShot = new IVideoPlayer14.PlayerSnapShot();
                snapShot.streamAddress = streamAddress2;
                snapShot.targetState = IVideoPlayer14.PlayerState.ERROR;
                snapShot.playPosition = 1000 * 60 * 3;
                snapShot.duration = 1000 * 1000 * 3;
                snapShot.error = new VideoPlayerException(new Bundle());
                snapShot.forceIsStreaming = false;
                snapShot.aspectRatio = IVideoPlayer14.AspectRatio.MATCH_CONTAINER;
                mVideoPlayer.restore(snapShot);*/
                /*IVideoPlayer14.PlayerSnapShot snapShot = new IVideoPlayer14.PlayerSnapShot();
                snapShot.streamAddress = streamAddress2;
                snapShot.targetState = IVideoPlayer14.PlayerState.PLAYBACK_COMPLETED;
                snapShot.playPosition = 1000 * 1000 * 3;
                snapShot.duration = 1000 * 1000 * 3;
                snapShot.error = null;
                snapShot.forceIsStreaming = false;
                snapShot.aspectRatio = IVideoPlayer14.AspectRatio.MATCH_CONTAINER;
                mVideoPlayer.restore(snapShot);*/
            }
        });
        vForceError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((VideoPlayerFragment) mVideoPlayer).forceError(-1, 1004);
            }
        });

        vAspectAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoPlayer.setAspectRatio(IVideoPlayer.AspectRatio.MATCH_CONTAINER);
            }
        });
        vAspect43.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoPlayer.setAspectRatio(IVideoPlayer.AspectRatio.ASPECT_4_3);
            }
        });
        vAspect169.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoPlayer.setAspectRatio(IVideoPlayer.AspectRatio.ASPECT_16_9);
            }
        });
        vAspect1610.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoPlayer.setAspectRatio(IVideoPlayer.AspectRatio.ASPECT_16_10);
            }
        });

        vPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVideoPlayer.getStreamAddress() == null) {
                    return;
                }

                switch (mVideoPlayer.getTargetPlayerState()) {
                    case PAUSED:
                    case STOPPED:
                    case PLAYBACK_COMPLETED:
                        mVideoPlayer.play();
                        break;
                    case STARTED:
                        mVideoPlayer.pause();
                        break;
                }
            }
        });
        vSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                mVideoPlayer.setPlayPosition(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        /*IVideoPlayer14.PlayerSnapShot snapShot = new IVideoPlayer14.PlayerSnapShot();
        snapShot.streamAddress = streamAddress2;
        snapShot.targetState = IVideoPlayer14.PlayerState.STARTED;
        snapShot.playPosition = 1000 * 60 * 3;
        snapShot.duration = 1000 * 1000 * 3;
        snapShot.error = null;
        snapShot.forceIsStreaming = false;
        snapShot.aspectRatio = IVideoPlayer14.AspectRatio.MATCH_CONTAINER;
        mVideoPlayer.restore(snapShot);*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoPlayer.unregisterOnWindowStateChangedListener(mOnWindowStateChangedListener);
        mVideoPlayer.unregisterOnPlayerStateChangedListener(mOnPlayerStateChangedListener);
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

    private class OnWindowStateChangedListenerImpl implements IVideoPlayer.OnWindowStateChangedListener {
        @Override
        public void onWindowStateChanged(IVideoPlayer player, IVideoPlayer.WindowState windowState) {
            switch (windowState) {
                case NOT_INITIALIZED:
                    vStream1.setEnabled(false);
                    vStream2.setEnabled(false);
                    vControls.setVisibility(View.GONE);
                    vProgress.setVisibility(View.GONE);
                    break;
                case INITIALIZING:
                    vProgress.setVisibility(View.VISIBLE);
                    break;
                case READY:
                    vStream1.setEnabled(true);
                    vStream2.setEnabled(true);
                    vControls.setVisibility(View.VISIBLE);
                    vProgress.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private class OnPlayerStateChangedListenerImpl implements IVideoPlayer.OnPlayerStateChangedListener {
        @Override
        public void onPlayerStateChanged(IVideoPlayer player, IVideoPlayer.PlayerState playerState) {
            switch (playerState) {
                case IDLE:
                    vControls.setVisibility(View.GONE);
                    break;
                case STOPPED:
                    vControls.setVisibility(View.VISIBLE);
                    vPlayPause.setText("Play");
                    break;
                case STARTED:
                    vControls.setVisibility(View.VISIBLE);
                    vPlayPause.setText("Pause");
                    break;
                case PAUSED:
                    vControls.setVisibility(View.VISIBLE);
                    vPlayPause.setText("Play");
                    break;
                case PLAYBACK_COMPLETED:
                    vControls.setVisibility(View.VISIBLE);
                    vPlayPause.setText("Repeat");
                    break;
                case ERROR:
                    vControls.setVisibility(View.GONE);
                    break;
            }

            /*CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "onPlayerStateChanged#state " + playerState.toString());
            CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "onPlayerStateChanged#duration " + player.getDuration());*/
            /*if (playerState != IVideoPlayer14.PlayerState.IDLE) {
                if (player.isLiveStreaming()) {
                    vSeekBar.setEnabled(false);
                } else {
                    vSeekBar.setEnabled(true);
                }
            }*/

        }

        @Override
        public void onBufferingStateChanged(IVideoPlayer player, IVideoPlayer.BufferingState bufferingState) {
            switch (bufferingState) {
                case BEGIN:
                    vProgress.setVisibility(View.VISIBLE);
                    break;
                case END:
                case CANCELLED:
                    vProgress.setVisibility(View.GONE);
                    break;
            }
        }

        @Override
        public void onVideoInfoReceived(IVideoPlayer player, boolean isStreaming, int duration) {
            CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "#onVideoInfoReceived(" + isStreaming + ", " + duration + ")");
            vControls.setVisibility(View.VISIBLE);
            if (isStreaming) {
                vSeekBar.setEnabled(false);
            } else {
                vSeekBar.setEnabled(true);
            }
        }

        @Override
        public void onPlayPositionChanged(IVideoPlayer player, int playPositionInMillis, int durationInMillis) {
            vPosition.setText("" + playPositionInMillis);
            vDuration.setText("" + durationInMillis);
            vSeekBar.setMax(durationInMillis);
            vSeekBar.setProgress(playPositionInMillis);
        }

        @Override
        public void onPlayPositionChangeRequested(IVideoPlayer player, int newPlayPositionInMillis, int durationInMillis) {

        }

        @Override
        public void onVolumeChanged(IVideoPlayer videoPlayer, int volumeCurrentLevel, int volumeMaxLevel) {
            Toast.makeText(getApplicationContext(), "Volume " + volumeCurrentLevel + "/" + volumeMaxLevel, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onAspectRatioChanged(IVideoPlayer videoPlayer, IVideoPlayer.AspectRatio aspectRatio) {
            Toast.makeText(getApplicationContext(), "Aspect " + aspectRatio.toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(IVideoPlayer player, VideoPlayerException ex) {
            mDialog = new AlertDialog.Builder(VideoPlayerTestActivity.this)
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
package com.uas.videoplayer.mediaplayerimpl;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.uas.videoplayer.interfaces.IVideoPlayer;
import com.uas.videoplayer.interfaces.VideoPlayerException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by a.sorokin@vectordigital.ru on 03.02.2015.
 *
 * @author a.sorokin@vectordigital.ru
 */
public class VideoPlayerFragment extends Fragment implements IVideoPlayer {

    public static final int DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;
    private static final String LOG_TAG = "MediaPlayer/UI";

    public static VideoPlayerFragment newInstance() {
        return new VideoPlayerFragment();
    }

    private WindowState mWindowState;
    private PlayerSnapShot mPlayerSnapShot;
    private PlayerState mCurrentState;

    private List<OnWindowStateChangedListener> mOnWindowStateChangedListeners;
    private List<OnPlayerStateChangedListener> mOnPlayerStateChangedListeners;

    private FrameLayoutWithSizeChangeListener mContainerView;
    private SurfaceView mSurfaceView;
    private SurfaceViewCallbacks mSurfaceViewCallbacks;
    private boolean mSurfaceViewIsCreated;
    private boolean mViewIsCreated;
    private int mBackgroundColor;

    private ExtendedMediaPlayer mMediaPlayer;
    private OnMediaPlayerExtendedEventsImpl mOnMediaPlayerExtendedEvents;
    private boolean mIsPreparing;
    private AudioManager mAudioManager;
    private VolumeChangeObserver mVolumeChangeObserver;

    private Handler mHandler;


    ////////////////////////////////////////////////////////////

    public VideoPlayerFragment() {
        super();

        mWindowState = WindowState.NOT_INITIALIZED;

        mPlayerSnapShot = new PlayerSnapShot();
        mPlayerSnapShot.targetState = PlayerState.IDLE;
        mPlayerSnapShot.streamAddress = null;
        mPlayerSnapShot.forceIsStreaming = false;
        mPlayerSnapShot.playPosition = 0;
        mPlayerSnapShot.duration = DURATION_FOR_LIVE;
        mPlayerSnapShot.aspectRatio = AspectRatio.MATCH_CONTAINER;
        mPlayerSnapShot.error = null;

        mCurrentState = PlayerState.IDLE;
        mIsPreparing = false;

        mOnWindowStateChangedListeners = new CopyOnWriteArrayList<OnWindowStateChangedListener>();
        mOnPlayerStateChangedListeners = new CopyOnWriteArrayList<OnPlayerStateChangedListener>();

        mSurfaceViewIsCreated = false;
        mViewIsCreated = false;
        mBackgroundColor = DEFAULT_BACKGROUND_COLOR;
    }

    @Override
    public void registerOnWindowStateChangedListener(OnWindowStateChangedListener onWindowStateChangedListener) {
        mOnWindowStateChangedListeners.add(onWindowStateChangedListener);
    }

    @Override
    public void registerOnPlayerStateChangedListener(OnPlayerStateChangedListener onPlayerStateChangedListener) {
        mOnPlayerStateChangedListeners.add(onPlayerStateChangedListener);
    }

    @Override
    public void unregisterOnWindowStateChangedListener(OnWindowStateChangedListener onWindowStateChangedListener) {
        mOnWindowStateChangedListeners.remove(onWindowStateChangedListener);
    }

    @Override
    public void unregisterOnPlayerStateChangedListener(OnPlayerStateChangedListener onPlayerStateChangedListener) {
        mOnPlayerStateChangedListeners.remove(onPlayerStateChangedListener);
    }

    public void forceError(int what, int extra) {
        mOnMediaPlayerExtendedEvents.onError(mMediaPlayer, what, extra);
    }


    ////////////////////////////////////////////////////////////


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "onCreate");

        mHandler = new Handler();

        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mVolumeChangeObserver = new VolumeChangeObserver(new Handler());
        getActivity().getContentResolver().registerContentObserver(
                android.provider.Settings.System.CONTENT_URI,
                true,
                mVolumeChangeObserver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "onCreateView");
        return inflater.inflate(R.layout.video_player_container, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "onViewCreated");
        mContainerView = (FrameLayoutWithSizeChangeListener) view.findViewById(R.id.container);
        mSurfaceView = (SurfaceView) view.findViewById(R.id.surface);
        mContainerView.setBackgroundColor(mBackgroundColor);
        mViewIsCreated = true;
    }

    @Override
    public void onStart() {
        super.onStart();
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "onStart");
        mContainerView.setOnSizeChangedListener(new OnContainerSizeChangedListener());
        onPlayerIsVisible();
    }

    @Override
    public void onStop() {
        super.onStop();
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "onStop");
        mContainerView.setOnSizeChangedListener(null);
        mHandler.removeCallbacksAndMessages(null);
        onPlayerIsHidden();
        onPlayerMustBeReleased();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "onDestroyView");
        mSurfaceView = null;
        mContainerView = null;
        mViewIsCreated = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "onDestroy");
        getActivity().getContentResolver().unregisterContentObserver(mVolumeChangeObserver);
        mVolumeChangeObserver = null;
        mAudioManager = null;
    }


    ////////////////////////////////////////////////////////////

    private void onPlayerIsVisible() {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "onPlayerIsVisible");

        mWindowState = WindowState.INITIALIZING;
        notifyOnWindowStateChanged(mWindowState);
        onWindowStateChanged();

        if (mMediaPlayer == null) {
            mMediaPlayer = new ExtendedMediaPlayer();
        }

        mSurfaceViewCallbacks = new SurfaceViewCallbacks();
        mSurfaceView.getHolder().addCallback(mSurfaceViewCallbacks);
        if (mSurfaceViewIsCreated) {
            mSurfaceViewCallbacks.onSurfaceCreated(mSurfaceView.getHolder());
        }
    }

    private void onPlayerIsHidden() {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "onPlayerIsHidden");
        if (mSurfaceViewIsCreated) {
            mSurfaceView.getHolder().removeCallback(mSurfaceViewCallbacks);
            mSurfaceViewCallbacks.onSurfaceDestroyed(mSurfaceView.getHolder());
        }
        mSurfaceViewCallbacks = null;
    }

    private void onPlayerMustBeReleased() {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "onPlayerMustBeReleased");
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(null);
            mMediaPlayer.setOnMediaPlayerExtendedEvents(null);
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private class SurfaceViewCallbacks implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceViewIsCreated = true;
            CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "surfaceCreated");
            onSurfaceCreated(holder);
        }

        public void onSurfaceCreated(SurfaceHolder holder) {
            mMediaPlayer.setDisplay(holder);
            mWindowState = WindowState.READY;

            mCurrentState = PlayerState.IDLE;
            changeAspectRatio(mPlayerSnapShot.aspectRatio.getRatio());

            notifyOnWindowStateChanged(mWindowState);
            notifyOnPlayerStateChanged(mCurrentState);

            mOnMediaPlayerExtendedEvents = new OnMediaPlayerExtendedEventsImpl();
            mMediaPlayer.setOnMediaPlayerExtendedEvents(mOnMediaPlayerExtendedEvents);

            onWindowStateChanged();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "surfaceChanged: " + format + "/" + width + "/" + height);
            changeAspectRatio(mPlayerSnapShot.aspectRatio.getRatio());
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "surfaceDestroyed");
            onSurfaceDestroyed(holder);
        }

        public void onSurfaceDestroyed(SurfaceHolder holder) {
            mSurfaceViewIsCreated = false;
            mOnMediaPlayerExtendedEvents = null;
            if (mMediaPlayer != null) {
                mMediaPlayer.setOnMediaPlayerExtendedEvents(null);
                if (mMediaPlayer.getState() != ExtendedMediaPlayer.State.IDLE) {
                    mMediaPlayer.reset();
                }
                mMediaPlayer.setDisplay(null);
            }

            if (mCurrentState != PlayerState.IDLE) {
                mCurrentState = PlayerState.IDLE;
                notifyOnPlayerStateChanged(mCurrentState);
            }

            mSurfaceView.getHolder().removeCallback(mSurfaceViewCallbacks);
            mWindowState = WindowState.NOT_INITIALIZED;
            notifyOnWindowStateChanged(mWindowState);

            onWindowStateChanged();
        }

    }

    private class OnContainerSizeChangedListener implements FrameLayoutWithSizeChangeListener.OnSizeChangedListener {
        @Override
        public void onSizeChanged(int oldWidth, int oldHeight, int newWidth, int newHeight) {
            changeAspectRatio(mPlayerSnapShot.aspectRatio.getRatio());
        }
    }

    private void onWindowStateChanged() {
        if (mWindowState == WindowState.NOT_INITIALIZED) {
            if (mMediaPlayer != null && mMediaPlayer.getState() != ExtendedMediaPlayer.State.IDLE) {
                mMediaPlayer.reset();
            }
        } else if (mWindowState == WindowState.READY) {
            if (mMediaPlayer.getState() != ExtendedMediaPlayer.State.IDLE) {
                mMediaPlayer.reset();
            }

            switch (mPlayerSnapShot.targetState) {
                case IDLE:
                    // due to mWindowState == WindowState.READY, the mCurrentState is already in IDLE state
                    // and listener was notified earlier
                    break;
                case STARTED:
                case PAUSED:
                case STOPPED:
                case PLAYBACK_COMPLETED:
                    launchPrepareOfStream();
                    break;
                case ERROR:
                    mCurrentState = PlayerState.ERROR;
                    notifyOnPlayerStateChanged(mCurrentState);
                    notifyOnError(mPlayerSnapShot.error);
                    break;
            }
        }
    }

    private void changeAspectRatio(float k) {
        int containerWidth;
        int containerHeight;
        if (mContainerView.getWidth() == 0) { // may be when view is not drawn yet
            containerWidth = mContainerView.getMeasuredWidth();
            containerHeight = mContainerView.getMeasuredHeight();
        } else {
            containerWidth = mContainerView.getWidth();
            containerHeight = mContainerView.getHeight();
        }
        float containerK = (float) containerWidth / (float) containerHeight;
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "changeAspectRatio: " + "/" + containerWidth + "/" + containerHeight);

        ViewGroup.LayoutParams lp = mSurfaceView.getLayoutParams();
        if (k < 0) {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mSurfaceView.setLayoutParams(lp);
        } else if (k < containerK) { // ~4/3 for 16:9/10 devices
            lp.width = (int) (containerHeight * k);
            lp.height = containerHeight;
            mSurfaceView.setLayoutParams(lp);
        } else { // ~16/9 for 16:9/10 devices
            lp.width = containerWidth;
            lp.height = (int) (containerWidth / k);
            mSurfaceView.setLayoutParams(lp);
        }
    }


    ////////////////////////////////////////////////////////////

    private class OnMediaPlayerExtendedEventsImpl implements ExtendedMediaPlayer.OnMediaPlayerExtendedEvents {
        @Override
        public void onStateChanged(ExtendedMediaPlayer mediaPlayer, ExtendedMediaPlayer.State newState) {
            switch (newState) {
                case IDLE:
                    mMediaPlayer.setScreenOnWhilePlaying(false);
                    if (mIsPreparing) {
                        mIsPreparing = false;
                        notifyOnBufferingStateChanged(BufferingState.CANCELLED);
                    }
                    if (mCurrentState != PlayerState.IDLE && mCurrentState != PlayerState.ERROR) {
                        mCurrentState = PlayerState.IDLE;
                        notifyOnPlayerStateChanged(mCurrentState);
                    }
                    break;

                case PREPARING:
                    mIsPreparing = true;
                    notifyOnBufferingStateChanged(BufferingState.BEGIN);
                    mMediaPlayer.setScreenOnWhilePlaying(false);
                    switch (mPlayerSnapShot.targetState) {
                        case IDLE:
                            mMediaPlayer.reset();
                            mCurrentState = PlayerState.IDLE;
                            notifyOnPlayerStateChanged(mCurrentState);
                            break;
                        case ERROR:
                            mMediaPlayer.reset();
                            mCurrentState = PlayerState.ERROR;
                            notifyOnPlayerStateChanged(mCurrentState);
                            notifyOnError(mPlayerSnapShot.error);
                            break;
                    }
                    break;

                case PREPARED:
                    if (mIsPreparing) {
                        mIsPreparing = false;
                        notifyOnBufferingStateChanged(BufferingState.END);
                    }

                    mMediaPlayer.setScreenOnWhilePlaying(false);

                    switch (mPlayerSnapShot.targetState) {
                        case IDLE:
                            mMediaPlayer.reset();
                            mCurrentState = PlayerState.IDLE;
                            notifyOnPlayerStateChanged(mCurrentState);
                            break;
                        case STARTED:
                        case PAUSED:
                        case STOPPED:
                        case PLAYBACK_COMPLETED:
                            //mPlayerSnapShot.playPosition = mMediaPlayer.getCurrentPosition();
                            mPlayerSnapShot.duration = mMediaPlayer.getDuration();
                            notifyOnVideoInfoReceived(
                                    isLiveStreaming(),
                                    mPlayerSnapShot.forceIsStreaming ? DURATION_FOR_LIVE : mPlayerSnapShot.duration);

                            if (mPlayerSnapShot.playPosition != 0 && !isLiveStreaming()) {
                                mMediaPlayer.seekTo(mPlayerSnapShot.playPosition);
                                notifyOnPlayPositionChanged(mPlayerSnapShot.playPosition, mPlayerSnapShot.duration);
                                /*mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mPlayerSnapShot.targetState == PlayerState.STARTED) {
                                            mMediaPlayer.seekTo(mPlayerSnapShot.playPosition);
                                            mMediaPlayer.start();
                                        }
                                    }
                                }, 10000);*/
                            }
                            if (mPlayerSnapShot.targetState == PlayerState.STARTED) {
                                mMediaPlayer.start();
                            } else {
                                notifyOnBufferingStateChanged(BufferingState.END);
                                mCurrentState = mPlayerSnapShot.targetState;
                                //mMediaPlayer.stop();
                                notifyOnPlayerStateChanged(mCurrentState);
                            }
                            break;
                        case ERROR:
                            mMediaPlayer.reset();
                            mCurrentState = PlayerState.ERROR;
                            notifyOnPlayerStateChanged(mCurrentState);
                            notifyOnError(mPlayerSnapShot.error);
                            break;
                    }

                    break;

                case STARTED:
                    switch (mPlayerSnapShot.targetState) {
                        case IDLE:
                            mMediaPlayer.reset();
                            mCurrentState = PlayerState.IDLE;
                            notifyOnPlayerStateChanged(mCurrentState);
                            break;
                        case STARTED:
                            mMediaPlayer.setScreenOnWhilePlaying(true);
                            mCurrentState = PlayerState.STARTED;
                            notifyOnPlayerStateChanged(mCurrentState);
                            break;
                        case PAUSED:
                            mMediaPlayer.pause();
                            mPlayerSnapShot.playPosition = mMediaPlayer.getCurrentPosition();
                            notifyOnPlayPositionChanged(mPlayerSnapShot.playPosition, mPlayerSnapShot.duration);
                            break;
                        case STOPPED:
                            mMediaPlayer.stop();
                            break;
                        case PLAYBACK_COMPLETED:
                            if (!isLiveStreaming()) {
                                mMediaPlayer.seekTo(mPlayerSnapShot.duration);
                            }
                            break;
                        case ERROR:
                            mMediaPlayer.reset();
                            mCurrentState = PlayerState.ERROR;
                            notifyOnPlayerStateChanged(mCurrentState);
                            notifyOnError(mPlayerSnapShot.error);
                            break;
                    }
                    break;

                case PAUSED:
                    switch (mPlayerSnapShot.targetState) {
                        case IDLE:
                            mMediaPlayer.reset();
                            mCurrentState = PlayerState.IDLE;
                            notifyOnPlayerStateChanged(mCurrentState);
                            break;
                        case STARTED:
                            mMediaPlayer.start();
                            break;
                        case PAUSED:
                            mMediaPlayer.setScreenOnWhilePlaying(false);
                            mCurrentState = PlayerState.PAUSED;
                            //mPlayerSnapShot.playPosition = mMediaPlayer.getCurrentPosition();
                            //notifyOnPlayPositionChanged(mPlayerSnapShot.playPosition, mPlayerSnapShot.duration);
                            notifyOnPlayerStateChanged(mCurrentState);
                            break;
                        case STOPPED:
                            mMediaPlayer.stop();
                            break;
                        case PLAYBACK_COMPLETED:
                            if (!isLiveStreaming()) {
                                mMediaPlayer.seekTo(mPlayerSnapShot.duration);
                            }
                            break;
                        case ERROR:
                            mMediaPlayer.reset();
                            mCurrentState = PlayerState.ERROR;
                            notifyOnPlayerStateChanged(mCurrentState);
                            notifyOnError(mPlayerSnapShot.error);
                            break;
                    }
                    break;

                case STOPPED:
                    switch (mPlayerSnapShot.targetState) {
                        case IDLE:
                            mMediaPlayer.reset();
                            mCurrentState = PlayerState.IDLE;
                            notifyOnPlayerStateChanged(mCurrentState);
                            break;
                        case STARTED:
                            launchPrepareOfStream();
                            break;
                        case PAUSED:
                            launchPrepareOfStream();
                            break;
                        case STOPPED:
                            mMediaPlayer.setScreenOnWhilePlaying(false);
                            mCurrentState = PlayerState.STOPPED;
                            notifyOnPlayerStateChanged(mCurrentState);
                            break;
                        case PLAYBACK_COMPLETED:
                            // nope, you don't want this
                            break;
                        case ERROR:
                            mMediaPlayer.reset();
                            mCurrentState = PlayerState.ERROR;
                            notifyOnPlayerStateChanged(mCurrentState);
                            notifyOnError(mPlayerSnapShot.error);
                            break;
                    }
                    break;

                case PLAYBACK_COMPLETED:
                    switch (mPlayerSnapShot.targetState) {
                        case IDLE:
                            mMediaPlayer.reset();
                            mCurrentState = PlayerState.IDLE;
                            notifyOnPlayerStateChanged(mCurrentState);
                            break;
                        /*case STARTED:
                            mMediaPlayer.seekTo(mPlayerSnapShot.playPosition);
                            mMediaPlayer.start();
                            break;
                        case PAUSED:
                            break;
                        case STOPPED:
                            mMediaPlayer.stop();
                            break;
                        case PLAYBACK_COMPLETED:
                            mMediaPlayer.setScreenOnWhilePlaying(false);
                            mCurrentState = PlayerState.PLAYBACK_COMPLETED;
                            notifyOnPlayerStateChanged(mCurrentState);
                            break;*/
                        case ERROR:
                            mMediaPlayer.reset();
                            mCurrentState = PlayerState.ERROR;
                            notifyOnPlayerStateChanged(mCurrentState);
                            notifyOnError(mPlayerSnapShot.error);
                            break;
                        default:
                            if (mCurrentState != PlayerState.PLAYBACK_COMPLETED) {
                                mMediaPlayer.setScreenOnWhilePlaying(false);
                                mCurrentState = PlayerState.PLAYBACK_COMPLETED;
                                mPlayerSnapShot.targetState = PlayerState.PLAYBACK_COMPLETED;
                                notifyOnPlayerStateChanged(mCurrentState);
                            }
                            break;
                    }
                    break;

                case ERROR:
                    // state will be dispatched at #onError callback
                    break;
            }
        }

        @Override
        public void onBufferingStateChanged(ExtendedMediaPlayer mediaPlayer, ExtendedMediaPlayer.BufferingState newBufferingState) {
            switch (newBufferingState) {
                case BEGIN:
                    notifyOnBufferingStateChanged(BufferingState.BEGIN);
                    break;
                case END:
                    notifyOnBufferingStateChanged(BufferingState.END);
                    break;
                case CANCELLED:
                    notifyOnBufferingStateChanged(BufferingState.CANCELLED);
                    break;
            }
        }

        @Override
        public void onPlayPositionChanged(ExtendedMediaPlayer mediaPlayer, int playPosition, int duration) {
            if (!isLiveStreaming()) {
                mPlayerSnapShot.playPosition = playPosition;
                mPlayerSnapShot.duration = duration;
                notifyOnPlayPositionChanged(playPosition, duration);
            }
        }

        @Override
        public void onError(ExtendedMediaPlayer mediaPlayer, int what, int extra) {
            mMediaPlayer.setScreenOnWhilePlaying(false);
            if (mIsPreparing) {
                notifyOnBufferingStateChanged(BufferingState.CANCELLED);
                mIsPreparing = false;
            }

            mCurrentState = PlayerState.ERROR;
            mPlayerSnapShot.targetState = PlayerState.ERROR;
            mPlayerSnapShot.error = createFromInternalErrors(what, extra);

            mMediaPlayer.reset();

            notifyOnPlayerStateChanged(mCurrentState);
            notifyOnError(mPlayerSnapShot.error);
        }
    }


    ////////////////////////////////////////////////////////////

    @Override
    public PlayerSnapShot takeSnapShot() {
        return new PlayerSnapShot(mPlayerSnapShot);
    }

    @Override
    public void restore(PlayerSnapShot snapshot) {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "restore: " + snapshot);

        if (getWindowState() == WindowState.READY) {
            mMediaPlayer.setOnMediaPlayerExtendedEvents(null);
            mMediaPlayer.reset();

            mPlayerSnapShot.updateFrom(snapshot);
            mMediaPlayer.setOnMediaPlayerExtendedEvents(mOnMediaPlayerExtendedEvents);

            switch (mPlayerSnapShot.targetState) {
                case IDLE:
                    mCurrentState = PlayerState.IDLE;
                    notifyOnPlayerStateChanged(mCurrentState);
                    break;
                case ERROR:
                    mCurrentState = PlayerState.ERROR;
                    notifyOnPlayerStateChanged(mCurrentState);
                    notifyOnError(mPlayerSnapShot.error);
                    break;
                default:
                    launchPrepareOfStream();
                    break;
            }
        } else {
            if (mMediaPlayer == null) {
                mPlayerSnapShot.updateFrom(snapshot);
            } else {
                mMediaPlayer.setOnMediaPlayerExtendedEvents(null);
                mMediaPlayer.reset();
                mPlayerSnapShot.updateFrom(snapshot);
                mMediaPlayer.setOnMediaPlayerExtendedEvents(mOnMediaPlayerExtendedEvents);
            }
        }
    }

    @Override
    public WindowState getWindowState() {
        return mWindowState;
    }

    @Override
    public PlayerState getTargetPlayerState() {
        return mPlayerSnapShot.targetState;
    }

    @Override
    public PlayerState getCurrentPlayerState() {
        return mCurrentState;
    }

    @Override
    public Uri getStreamAddress() {
        return mPlayerSnapShot.streamAddress;
    }

    @Override
    public int getPlayPosition() {
        return mPlayerSnapShot.playPosition;
    }

    @Override
    public int getDuration() {
        return mPlayerSnapShot.duration;
    }

    @Override
    public AspectRatio getAspectRatio() {
        return mPlayerSnapShot.aspectRatio;
    }

    @Override
    public VideoPlayerException getError() {
        return mPlayerSnapShot.error;
    }

    @Override
    public boolean isLiveStreaming() {
        return mPlayerSnapShot.forceIsStreaming || mPlayerSnapShot.duration == DURATION_FOR_LIVE;
    }

    @Override
    public int getVolumeMaxLevel() {
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public int getVolumeCurrentLevel() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }


    ////////////////////////////////////////////////////////////

    @Override
    public void changeStreamAddress(Uri streamAddress) {
        changeStreamAddress(streamAddress, false);
    }

    @Override
    public void changeStreamAddress(Uri streamAddress, boolean forceIsStreaming) {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "changeStreamAddress: " + forceIsStreaming + " / "+ streamAddress);
        boolean mockStreamAddress = streamAddress != null && streamAddress.toString().equals("1");

        if (mMediaPlayer != null && mMediaPlayer.getState() != ExtendedMediaPlayer.State.IDLE) {
            //if (!mockStreamAddress) {
                mMediaPlayer.reset();
            //}
        }

        if (mockStreamAddress) {
            streamAddress = null;
        }

        mPlayerSnapShot.streamAddress = streamAddress;
        mPlayerSnapShot.forceIsStreaming = forceIsStreaming;
        mPlayerSnapShot.playPosition = 0;
        mPlayerSnapShot.targetState = PlayerState.STARTED;

        mCurrentState = PlayerState.IDLE;
        notifyOnPlayerStateChanged(mCurrentState);

        if (getWindowState() == WindowState.READY) {
            if (!mockStreamAddress) {
                launchPrepareOfStream();
            }
        }
    }

    @Override
    public void play() {
        if (getWindowState() == WindowState.READY) {
            if (mCurrentState == PlayerState.ERROR) {
                mCurrentState = PlayerState.IDLE;
                mPlayerSnapShot.targetState = PlayerState.STARTED;
                mPlayerSnapShot.error = null;
                notifyOnPlayerStateChanged(mCurrentState);
                launchPrepareOfStream();
                return;
            }

            if (mCurrentState == PlayerState.PLAYBACK_COMPLETED) {
                mPlayerSnapShot.targetState = PlayerState.STARTED;
                if (!isLiveStreaming()) {
                    mPlayerSnapShot.playPosition = 0;
                    notifyOnPlayPositionChanged(mPlayerSnapShot.playPosition, mPlayerSnapShot.duration);
                    mMediaPlayer.seekTo(0);
                }
                mMediaPlayer.start();
                return;
            }

            // TODO use current state?
            switch (mMediaPlayer.getState()) {
                case IDLE: // possibly we can remove this
                case INITIALIZED:
                    if (mCurrentState != PlayerState.IDLE) {
                        mCurrentState = PlayerState.IDLE;
                        notifyOnPlayerStateChanged(mCurrentState);
                    }
                    mPlayerSnapShot.playPosition = 0;
                    mPlayerSnapShot.targetState = PlayerState.STARTED;
                    notifyOnPlayPositionChanged(mPlayerSnapShot.playPosition, mPlayerSnapShot.duration);
                    launchPrepareOfStream();
                    break;
                case PREPARING:
                    mPlayerSnapShot.targetState = PlayerState.STARTED;
                    break;
                case STARTED:
                    mPlayerSnapShot.targetState = PlayerState.STARTED;
                    break;
                case PREPARED:
                case PAUSED:
                    mPlayerSnapShot.targetState = PlayerState.STARTED;
                    if (mMediaPlayer.getCurrentPosition() != mPlayerSnapShot.playPosition && !isLiveStreaming()) {
                        //mPlayerSnapShot.playPosition = mMediaPlayer.getCurrentPosition();
                        mMediaPlayer.seekTo(mPlayerSnapShot.playPosition);
                    }
                    mMediaPlayer.start();
                    break;
                case STOPPED:
                    mPlayerSnapShot.playPosition = 0;
                    mPlayerSnapShot.targetState = PlayerState.STARTED;
                    notifyOnPlayPositionChanged(mPlayerSnapShot.playPosition, mPlayerSnapShot.duration);
                    launchPrepareOfStream();
                    break;
                case PLAYBACK_COMPLETED:
                    mPlayerSnapShot.targetState = PlayerState.STARTED;
                    if (!isLiveStreaming()) {
                        mPlayerSnapShot.playPosition = 0;
                        notifyOnPlayPositionChanged(mPlayerSnapShot.playPosition, mPlayerSnapShot.duration);
                        mMediaPlayer.seekTo(0);
                    }
                    mMediaPlayer.start();
                    break;
                case ERROR:
                    mMediaPlayer.reset();
                    mPlayerSnapShot.playPosition = 0;
                    mPlayerSnapShot.targetState = PlayerState.STARTED;
                    mPlayerSnapShot.error = null;
                    notifyOnPlayPositionChanged(mPlayerSnapShot.playPosition, mPlayerSnapShot.duration);
                    launchPrepareOfStream();
                    break;
            }
        } else {
            if (mPlayerSnapShot.streamAddress != null) {
                mPlayerSnapShot.targetState = PlayerState.STARTED;
            }
        }
    }

    @Override
    public void pause() {
        if (getWindowState() == WindowState.READY) {
            switch (mMediaPlayer.getState()) {
                case IDLE:
                case INITIALIZED:
                case PREPARING:
                    mPlayerSnapShot.targetState = PlayerState.PAUSED;
                    break;
                case PREPARED:
                    mPlayerSnapShot.targetState = PlayerState.PAUSED;
                    if (!isLiveStreaming()) {
                        mMediaPlayer.seekTo(mPlayerSnapShot.playPosition);
                    }
                    break;
                case STARTED:
                    mPlayerSnapShot.targetState = PlayerState.PAUSED;
                    //mPlayerSnapShot.playPosition = mMediaPlayer.getCurrentPosition();
                    //notifyOnPlayPositionChanged(mPlayerSnapShot.playPosition, mPlayerSnapShot.duration);
                    mMediaPlayer.pause();
                    break;
                case PAUSED:
                case PLAYBACK_COMPLETED:
                case ERROR:
                    break;
            }
        } else {
            mPlayerSnapShot.targetState = PlayerState.PAUSED;
        }
    }

    @Override
    public void stop() {
        if (getWindowState() == WindowState.READY) {
            switch (mMediaPlayer.getState()) {
                case IDLE:
                case INITIALIZED:
                case PREPARING:
                    mPlayerSnapShot.playPosition = 0;
                    mPlayerSnapShot.targetState = PlayerState.STOPPED;
                    break;
                case PREPARED:
                case STARTED:
                case PAUSED:
                case PLAYBACK_COMPLETED:
                    mPlayerSnapShot.targetState = PlayerState.STOPPED;
                    if (!isLiveStreaming()) {
                        mPlayerSnapShot.playPosition = 0;
                        notifyOnPlayPositionChanged(mPlayerSnapShot.playPosition, mPlayerSnapShot.duration);
                        mMediaPlayer.seekTo(0);
                    }
                    mMediaPlayer.stop();
                    break;
                case STOPPED:
                case ERROR:
                    break;
            }
        } else {
            mPlayerSnapShot.targetState = PlayerState.STOPPED;
        }
    }

    @Override
    public void setPlayPosition(int playPosition) {
        if (getWindowState() == WindowState.READY) {
            switch (mMediaPlayer.getState()) {
                case IDLE:
                case INITIALIZED:
                case PREPARING:
                    mPlayerSnapShot.playPosition = playPosition;
                    notifyOnPlayPositionChanged(mPlayerSnapShot.playPosition, mPlayerSnapShot.duration);
                    break;
                case PREPARED:
                case STARTED:
                case PAUSED:
                    if (!isLiveStreaming()) {
                        mMediaPlayer.seekTo(playPosition);
                        mPlayerSnapShot.playPosition = playPosition;
                        notifyOnPlayPositionChanged(mPlayerSnapShot.playPosition, mPlayerSnapShot.duration);
                    }
                    break;
                case PLAYBACK_COMPLETED:
                    break;
                case ERROR:
                    break;
            }
        } else {
            mPlayerSnapShot.playPosition = playPosition;
        }
        notifyOnPlayPositionChangeRequested(playPosition, getDuration());
    }

    @Override
    public void setAspectRatio(AspectRatio aspectRatio) {
        mPlayerSnapShot.aspectRatio = aspectRatio;
        if (getWindowState() == WindowState.READY) {
            changeAspectRatio(mPlayerSnapShot.aspectRatio.getRatio());
        }
        notifyOnAspectRadioChanged(mPlayerSnapShot.aspectRatio);
    }

    @Override
    public void setVolumeCurrentLevel(int volume) {
        if (volume > getVolumeMaxLevel()) {
            volume = getVolumeMaxLevel();
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        notifyOnVolumeChanged(getVolumeCurrentLevel(), getVolumeMaxLevel());
    }

    @Override
    public void setBackgroundColor(int color) {
        mBackgroundColor = color;
        if (mViewIsCreated) {
            mContainerView.setBackgroundColor(mBackgroundColor);
        }
    }

    @Override
    public int getBackgroundColor() {
        return mBackgroundColor;
    }


    ////////////////////////////////////////////////////////////

    private void launchPrepareOfStream() {
        if (mPlayerSnapShot.streamAddress == null) {
            if (mCurrentState != PlayerState.IDLE) {
                mCurrentState = PlayerState.IDLE;
                notifyOnPlayerStateChanged(mCurrentState);
            }
        } else {
            try {
                if (mMediaPlayer.getState() != ExtendedMediaPlayer.State.IDLE) {
                    mMediaPlayer.reset();
                }
                if (mCurrentState != PlayerState.IDLE) {
                    mCurrentState = PlayerState.IDLE;
                    notifyOnPlayerStateChanged(mCurrentState);
                }
                mMediaPlayer.setDataSource(getActivity().getApplicationContext(), mPlayerSnapShot.streamAddress, mPlayerSnapShot.forceIsStreaming);
                mMediaPlayer.prepareAsync();
            } catch (IOException ex) {
                mCurrentState = PlayerState.ERROR;
                mPlayerSnapShot.targetState = PlayerState.ERROR;
                mPlayerSnapShot.error = new VideoPlayerException(ex);
                notifyOnPlayerStateChanged(mCurrentState);
                notifyOnError(mPlayerSnapShot.error);
            }
        }
    }


    ////////////////////////////////////////////////////////////

    private class VolumeChangeObserver extends ContentObserver {

        private int mPreviousValue;

        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */
        public VolumeChangeObserver(Handler handler) {
            super(handler);
            mPreviousValue = getVolumeCurrentLevel();
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            AudioManager audio = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mPreviousValue != currentVolume) {
                setVolumeCurrentLevel(currentVolume);
                mPreviousValue = getVolumeCurrentLevel();
            }
        }

    }


    ////////////////////////////////////////////////////////////

    private VideoPlayerException createFromInternalErrors(int what, int extra) {
        Bundle bundle = new Bundle();
        bundle.putInt("what", what);
        bundle.putInt("extra", extra);
        return new VideoPlayerException(bundle);
    }


    ////////////////////////////////////////////////////////////

    private void notifyOnWindowStateChanged(WindowState windowState) {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "notifyOnWindowStateChanged: " + windowState.toString());
        for (OnWindowStateChangedListener listener: mOnWindowStateChangedListeners) {
            listener.onWindowStateChanged(this, windowState);
        }
    }

    private void notifyOnPlayerStateChanged(PlayerState playerState) {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, 2, "notifyOnPlayerStateChanged: " + playerState.toString());
        for (OnPlayerStateChangedListener listener: mOnPlayerStateChangedListeners) {
            listener.onPlayerStateChanged(this, playerState);
        }
    }

    private void notifyOnBufferingStateChanged(BufferingState bufferingState) {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "notifyOnBufferingStateChanged: " + bufferingState.toString());
        for (OnPlayerStateChangedListener listener: mOnPlayerStateChangedListeners) {
            listener.onBufferingStateChanged(this, bufferingState);
        }
    }

    private void notifyOnVideoInfoReceived(boolean isStreaming, int duration) {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "notifyOnVideoInfoReceived: " + isStreaming + ", " + duration);
        for (OnPlayerStateChangedListener listener: mOnPlayerStateChangedListeners) {
            listener.onVideoInfoReceived(this, isStreaming, duration);
        }
    }

    private void notifyOnPlayPositionChanged(int playPositionInMillis, int durationInMillis) {
        for (OnPlayerStateChangedListener listener: mOnPlayerStateChangedListeners) {
            listener.onPlayPositionChanged(this, playPositionInMillis, durationInMillis);
        }
    }

    private void notifyOnPlayPositionChangeRequested(int newPlayPositionInMillis, int durationInMillis) {
        for (OnPlayerStateChangedListener listener: mOnPlayerStateChangedListeners) {
            listener.onPlayPositionChangeRequested(this, newPlayPositionInMillis, durationInMillis);
        }
    }

    private void notifyOnVolumeChanged(int volumeCurrentLevel, int volumeMaxLevel) {
        for (OnPlayerStateChangedListener listener: mOnPlayerStateChangedListeners) {
            listener.onVolumeChanged(this, volumeCurrentLevel, volumeMaxLevel);
        }
    }

    private void notifyOnAspectRadioChanged(AspectRatio aspectRatio) {
        for (OnPlayerStateChangedListener listener: mOnPlayerStateChangedListeners) {
            listener.onAspectRatioChanged(this, aspectRatio);
        }
    }

    private void notifyOnError(VideoPlayerException ex) {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, "notifyOnError: " + ex.toString());
        for (OnPlayerStateChangedListener listener: mOnPlayerStateChangedListeners) {
            listener.onError(this, ex);
        }
    }

}
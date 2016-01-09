package com.uas.videoplayer.mediaplayerimpl;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Map;

/**
 * Created by a.sorokin@vectordigital.ru on 05.02.2015.
 * @author a.sorokin@vectordigital.ru
 * TODO do support of #isLooping
 *
 * See MediaPlayer errors here: http://www.virtsync.com/c-error-codes-include-errno
 */
final public class ExtendedMediaPlayer extends MediaPlayer {

    public static final int LIVE_STREAMING_DURATION = -1;
    private static final String LOG_TAG = "MediaPlayer/Extended";

    /**
     * On some devices the complete event is not delivered, because
     * internal player hangs right before the end (~2 seconds before end)
     */
    private static final int OFFSET_FROM_END_TO_DETECT_COMPLETE = 1000 * 2;

    /**
     * Interval for state observer in millis
     */
    private static final int OBSERVER_INVOKE_TIME = 200;



    /**
     * We are using our local state instead of framework's one.
     * The reasons:
     * 1) Provide states to contributors, because original MediaPlayer's states are hidden
     * 2) In some cases original MediaPlayer throws IllegalStateException between state change commands,
     *    sometimes it says about error in OnErrorListener. This class has a stable usage of
     *    IllegalStateException, not events.
     * 3) Some devices would not to send onComplete and hangs. This class resolves such error via observer.
     */
    public enum State {
        IDLE,
        INITIALIZED,
        PREPARED,
        PREPARING,
        STARTED,
        PAUSED,
        STOPPED,
        PLAYBACK_COMPLETED,
        ERROR,
        RELEASED
    }

    public enum BufferingState {
        BEGIN,
        END,
        CANCELLED
    }

    public interface OnMediaPlayerExtendedEvents {
        void onStateChanged(ExtendedMediaPlayer mediaPlayer, State newState);
        void onBufferingStateChanged(ExtendedMediaPlayer mediaPlayer, BufferingState newBufferingState);
        void onPlayPositionChanged(ExtendedMediaPlayer mediaPlayer, int playPosition, int duration);
        void onError(ExtendedMediaPlayer mediaPlayer, int what, int extra);
    }



    private State mState;
    private Handler mObserverHandler;
    private PlayerObserver mObserverTask;
    private int mObserverLastPlayPosition;
    private boolean mObserverIsBuffering;
    private boolean mForceIsStreaming;
    private int mTargetPlayPosition; // workaround for handling jump of position at observer

    private OnPreparedListener mOnPreparedListener;
    private OnCompletionListener mOnCompletionListener;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private OnMediaPlayerExtendedEvents mOnMediaPlayerExtendedEvents;

    public ExtendedMediaPlayer() {
        super();
        mState = State.IDLE;
        mObserverHandler = new Handler();
        mObserverTask = new PlayerObserver();
        mObserverLastPlayPosition = 0;
        mObserverIsBuffering = false;
        mForceIsStreaming = false;
        mTargetPlayPosition = -1;

        super.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mState = State.PREPARED;
                notifyBaseOnPrepared();
                notifyOnStateChanged(mState);
            }
        });
        super.setOnCompletionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // it's okay, because Android sometimes invokes this callback right after the error
                // (i.e. when network file/stream (HLS in current case) is not found)
                if (getState() != State.ERROR) {
                    safeAndCorrectStopTheObserver();
                    mState = State.PLAYBACK_COMPLETED;
                    notifyBaseOnCompletion();
                    notifyOnStateChanged(mState);
                }
            }
        });
        super.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mState = State.ERROR;
                notifyBaseOnError(what, extra);
                notifyOnStateChanged(mState);
                notifyOnError(what, extra);
                return true;
            }
        });
        super.setOnInfoListener(new OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                boolean isProcessed = false;
                switch (what) {
                    case MEDIA_INFO_BUFFERING_START:
                        isProcessed = true;
                        if (!mObserverIsBuffering) {
                            mObserverIsBuffering = true;
                            notifyOnBufferingStateChanged(BufferingState.BEGIN);
                        }
                        break;
                    case MEDIA_INFO_BUFFERING_END:
                        isProcessed = true;
                        if (mObserverIsBuffering) {
                            mObserverIsBuffering = false;
                            notifyOnBufferingStateChanged(BufferingState.END);
                        }
                        break;
                    default:
                        break;
                }

                notifyBaseOnInfoListener(what, extra);

                return isProcessed;
            }
        });
    }

    public State getState() {
        return mState;
    }

    @Override
    public void setOnPreparedListener(OnPreparedListener onPreparedListener) {
        mOnPreparedListener = onPreparedListener;
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener onCompletionListener) {
        mOnCompletionListener = onCompletionListener;
    }

    @Override
    public void setOnErrorListener(OnErrorListener onErrorListener) {
        mOnErrorListener = onErrorListener;
    }

    @Override
    public void setOnInfoListener(OnInfoListener listener) {
        mOnInfoListener = listener;
    }

    public void setOnMediaPlayerExtendedEvents(OnMediaPlayerExtendedEvents onMediaPlayerExtendedEvents) {
        mOnMediaPlayerExtendedEvents = onMediaPlayerExtendedEvents;
    }

    public boolean isStreaming() {
        switch (getState()) {
            case PREPARED:
            case STARTED:
            case PAUSED:
                return mForceIsStreaming || getDuration() == LIVE_STREAMING_DURATION;
            default:
                return false;
        }
    }

    public boolean isStreamInfoAvailable() {
        switch (getState()) {
            case PREPARED:
            case STARTED:
            case PAUSED:
            case PLAYBACK_COMPLETED:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void prepare() throws IOException, IllegalStateException {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, 1, "mp#prepare");
        switch (getState()) {
            case INITIALIZED:
            case STOPPED:
                super.prepare();
                // State.PREPARED will be set in internal listener defined at constructor
                mState = State.PREPARING;
                notifyOnStateChanged(mState);
                break;
            default:
                throw new IllegalStateException("prepare() called from invalid state: " + getState().toString());
        }
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, 1, "mp#prepareAsync");
        switch (getState()) {
            case INITIALIZED:
            case STOPPED:
                super.prepareAsync();
                // State.PREPARED will be set in internal listener defined at constructor
                mState = State.PREPARING;
                notifyOnStateChanged(mState);
                break;
            default:
                throw new IllegalStateException("prepareAsync() called from invalid state: " + getState().toString());
        }
    }

    @Override
    public void start() throws IllegalStateException {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, 1, "mp#start");
        switch (getState()) {
            case STARTED:
                // ignore
                break;
            case PREPARED:
            case PAUSED:
            case PLAYBACK_COMPLETED:
                super.start();
                mState = State.STARTED;
                notifyOnStateChanged(mState);
                if (!isStreaming()) {
                    safeStartTheObserver();
                }
                break;
            default:
                throw new IllegalStateException("start() called from invalid state: " + getState().toString());
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, 1, "mp#pause");
        switch (getState()) {
            case PAUSED:
                // ignore
                break;
            case STARTED:
                super.pause();
                safeAndCorrectStopTheObserver();
                mState = State.PAUSED;
                notifyOnStateChanged(mState);
                break;
            default:
                throw new IllegalStateException("pause() called from invalid state: " + getState().toString());
        }
    }

    @Override
    public void stop() throws IllegalStateException {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, 1, "mp#stop");
        switch (getState()) {
            case STOPPED:
                // ignore
                break;
            case PREPARED:
            case STARTED:
            case PAUSED:
            case PLAYBACK_COMPLETED:
                super.stop();
                safeAndCorrectStopTheObserver();
                mState = State.STOPPED;
                notifyOnStateChanged(mState);
                break;
            default:
                throw new IllegalStateException("stop() called from invalid state: " + getState().toString());
        }
    }

    @Override
    public void reset() {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, 1, "mp#reset");
        super.reset();
        safeAndCorrectStopTheObserver();
        mState = State.IDLE;
        notifyOnStateChanged(mState);
    }

    @Override
    public void release() {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, 1, "mp#release");
        super.release();
        safeAndCorrectStopTheObserver();
        mState = State.RELEASED;
        notifyOnStateChanged(mState);
    }

    @Override
    public void setDataSource(Context context, Uri uri) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(context, uri, false);
    }

    @Override
    public void setDataSource(Context context, Uri uri, Map<String, String> headers) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(context, uri, headers, false);
    }

    @Override
    public void setDataSource(String path) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        setDataSource(path, false);
    }

    @Override
    public void setDataSource(FileDescriptor fd) throws IOException, IllegalArgumentException, IllegalStateException {
        setDataSource(fd, false);
    }

    @Override
    public void setDataSource(FileDescriptor fd, long offset, long length) throws IOException, IllegalArgumentException, IllegalStateException {
        setDataSource(fd, offset, length, false);
    }


    public void setDataSource(Context context, Uri uri, boolean forceIsStreaming) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if (getState() == State.IDLE) {
            super.setDataSource(context, uri);
            CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, 1, "mp#setDataSource(" + uri + ")");
            mForceIsStreaming = forceIsStreaming;
            mState = State.INITIALIZED;
            notifyOnStateChanged(mState);
        } else {
            throw new IllegalStateException("setDataSource() called from invalid state: " + getState().toString());
        }
    }

    public void setDataSource(Context context, Uri uri, Map<String, String> headers, boolean forceIsStreaming) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if (getState() == State.IDLE) {
            super.setDataSource(context, uri, headers);
            mForceIsStreaming = forceIsStreaming;
            mState = State.INITIALIZED;
            notifyOnStateChanged(mState);
        } else {
            throw new IllegalStateException("setDataSource() called from invalid state: " + getState().toString());
        }
    }

    public void setDataSource(String path, boolean forceIsStreaming) throws IOException, IllegalArgumentException, SecurityException, IllegalStateException {
        if (getState() == State.IDLE) {
            super.setDataSource(path);
            mForceIsStreaming = forceIsStreaming;
            mState = State.INITIALIZED;
            notifyOnStateChanged(mState);
        } else {
            throw new IllegalStateException("setDataSource() called from invalid state: " + getState().toString());
        }
    }

    public void setDataSource(FileDescriptor fd, boolean forceIsStreaming) throws IOException, IllegalArgumentException, IllegalStateException {
        if (getState() == State.IDLE) {
            super.setDataSource(fd);
            mForceIsStreaming = forceIsStreaming;
            mState = State.INITIALIZED;
            notifyOnStateChanged(mState);
        } else {
            throw new IllegalStateException("setDataSource() called from invalid state: " + getState().toString());
        }
    }

    public void setDataSource(FileDescriptor fd, long offset, long length, boolean forceIsStreaming) throws IOException, IllegalArgumentException, IllegalStateException {
        if (getState() == State.IDLE) {
            super.setDataSource(fd, offset, length);
            mForceIsStreaming = forceIsStreaming;
            mState = State.INITIALIZED;
            notifyOnStateChanged(mState);
        } else {
            throw new IllegalStateException("setDataSource() called from invalid state: " + getState().toString());
        }
    }

    @Override
    public void seekTo(int msec) throws IllegalStateException {
        CalleeLogInfo.detectAndDumpToLogCat(LOG_TAG, 1, "mp#seekTo(" + msec + ")");
        switch (getState()) {
            case STARTED:
                super.seekTo(msec);
                mObserverLastPlayPosition = msec;
                mTargetPlayPosition = msec;
                break;
            case PREPARED:
            case PAUSED:
                super.seekTo(msec);
                mObserverLastPlayPosition = msec;
                mTargetPlayPosition = msec;
                if (!mObserverIsBuffering) {
                    mObserverIsBuffering = true;
                    notifyOnBufferingStateChanged(BufferingState.BEGIN);
                }
                break;
            case PLAYBACK_COMPLETED:
                super.seekTo(msec);
                break;
            default:
                throw new IllegalStateException("seekTo() called from invalid state: " + getState().toString());
        }
        /*switch (getState()) {
            case PREPARED:
            case STARTED:
            case PAUSED:
            case PLAYBACK_COMPLETED:
                super.seekTo(msec);
                //mObserverLastPlayPosition = msec;
                break;
            default:
                throw new IllegalStateException("seekTo() called from invalid state: " + getState().toString());
        }*/
    }

    private void safeStartTheObserver() {
        mObserverHandler.removeCallbacks(mObserverTask);

        mObserverLastPlayPosition = getCurrentPosition();
        mObserverIsBuffering = false;
        mObserverHandler.postDelayed(mObserverTask, OBSERVER_INVOKE_TIME);
    }

    private void safeAndCorrectStopTheObserver() {
        mObserverHandler.removeCallbacks(mObserverTask);

        if (mObserverIsBuffering) {
            mObserverIsBuffering = false;
            notifyOnBufferingStateChanged(BufferingState.CANCELLED);
        }
        mObserverLastPlayPosition = 0;
    }

    private class PlayerObserver implements Runnable {
        @Override
        public void run() {
            if (getState() == State.STARTED
                    && mObserverLastPlayPosition == getCurrentPosition()
                    && !mObserverIsBuffering) {
                mObserverIsBuffering = true;
                notifyOnBufferingStateChanged(BufferingState.BEGIN);
            }

            if (getState() == State.STARTED
                    && mObserverLastPlayPosition != getCurrentPosition()
                    && mObserverIsBuffering) {
                mObserverIsBuffering = false;
                notifyOnBufferingStateChanged(BufferingState.END);
            }
            mObserverLastPlayPosition = getCurrentPosition();

            if (getState() == State.STARTED && !mObserverIsBuffering) {
                if (mTargetPlayPosition > 0) {
                    notifyOnPlayPositionChanged(mTargetPlayPosition, getDuration());
                    mTargetPlayPosition = -1;
                } else {
                    notifyOnPlayPositionChanged(getCurrentPosition(), getDuration());
                }
            }

            // workaround to avoid some hangs before end on some devices
            if (getState() == State.STARTED
                    && !mObserverIsBuffering
                    && getDuration() >= 0
                    && getDuration() - mObserverLastPlayPosition <= OFFSET_FROM_END_TO_DETECT_COMPLETE) {
                pause();
                //seekTo(getDuration());
                notifyOnPlayPositionChanged(getDuration(), getDuration());
                //stop();
                //reset();
                mState = State.PLAYBACK_COMPLETED;
                notifyBaseOnCompletion();
                notifyOnStateChanged(mState);
            }

            mObserverHandler.postDelayed(this, OBSERVER_INVOKE_TIME);
        }
    }


    // ======================================================================

    private void notifyBaseOnPrepared() {
        if (mOnPreparedListener != null) {
            mOnPreparedListener.onPrepared(ExtendedMediaPlayer.this);
        }
    }

    private void notifyBaseOnCompletion() {
        if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(ExtendedMediaPlayer.this);
        }
    }

    private void notifyBaseOnError(int what, int extra) {
        // emulate default docs behaviour: http://developer.android.com/reference/android/media/MediaPlayer.OnErrorListener.html
        boolean deliverCompletedAsDocsSays;
        if (mOnErrorListener != null) {
            deliverCompletedAsDocsSays = !mOnErrorListener.onError(ExtendedMediaPlayer.this, what, extra);
        } else {
            deliverCompletedAsDocsSays = true;
        }
        if (deliverCompletedAsDocsSays && mOnCompletionListener != null) {
            notifyBaseOnCompletion();
        }
    }

    private void notifyBaseOnInfoListener(int what, int extra) {
        if (mOnInfoListener != null) {
            mOnInfoListener.onInfo(ExtendedMediaPlayer.this, what, extra);
        }
    }


    // ======================================================================

    private void notifyOnStateChanged(State newState) {
        if (mOnMediaPlayerExtendedEvents != null) {
            mOnMediaPlayerExtendedEvents.onStateChanged(this, newState);
        }
    }

    private void notifyOnBufferingStateChanged(BufferingState bufferingState) {
        if (mOnMediaPlayerExtendedEvents != null) {
            mOnMediaPlayerExtendedEvents.onBufferingStateChanged(this, bufferingState);
        }
    }

    private void notifyOnPlayPositionChanged(int playPosition, int duration) {
        if (mOnMediaPlayerExtendedEvents != null) {
            mOnMediaPlayerExtendedEvents.onPlayPositionChanged(this, playPosition, duration);
        }
    }

    private void notifyOnError(int what, int extra) {
        if (mOnMediaPlayerExtendedEvents != null) {
            mOnMediaPlayerExtendedEvents.onError(this, what, extra);
        }
    }

}
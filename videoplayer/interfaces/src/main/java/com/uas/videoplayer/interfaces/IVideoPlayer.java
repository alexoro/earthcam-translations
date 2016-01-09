package com.uas.videoplayer.interfaces;

import android.net.Uri;

import java.io.Serializable;

/**
 * Created by a.sorokin@vectordigital.ru on 09.02.2015.
 *
 * @author a.sorokin@vectordigital.ru
 */
public interface IVideoPlayer {

    enum WindowState {
        NOT_INITIALIZED,
        INITIALIZING,
        READY
    }

    enum PlayerState {
        IDLE,
        STARTED,
        PAUSED,
        STOPPED,
        PLAYBACK_COMPLETED,
        ERROR
    }

    enum BufferingState {
        BEGIN,
        END,
        CANCELLED
    }

    enum AspectRatio {
        MATCH_CONTAINER(-1f),
        ASPECT_4_3(4f/3f),
        ASPECT_16_9(16f/9f),
        ASPECT_16_10(16f/10f),;

        private float mRatio;

        AspectRatio(float ratio) {
            mRatio = ratio;
        }

        public float getRatio() {
            return mRatio;
        }
    }

    int DURATION_FOR_LIVE = -1;

    static class PlayerSnapShot implements Serializable {

        private static final long serialVersionUID = 2449228176372521718L;

        public PlayerState targetState;
        public Uri streamAddress;
        public boolean forceIsStreaming;
        public int playPosition;
        public int duration;
        public AspectRatio aspectRatio;
        public VideoPlayerException error;

        public PlayerSnapShot() {

        }

        public PlayerSnapShot(PlayerSnapShot copyFrom) {
            updateFrom(copyFrom);
        }

        public void updateFrom(PlayerSnapShot copyFrom) {
            targetState = copyFrom.targetState;
            streamAddress = copyFrom.streamAddress;
            forceIsStreaming = copyFrom.forceIsStreaming;
            playPosition = copyFrom.playPosition;
            duration = copyFrom.duration;
            aspectRatio = copyFrom.aspectRatio;
            error = copyFrom.error;
        }

        @Override
        public String toString() {
            return "PlayerSnapShot{" +
                    "targetState=" + targetState +
                    ", streamAddress=" + streamAddress +
                    ", forceIsStreaming=" + forceIsStreaming +
                    ", playPosition=" + playPosition +
                    ", duration=" + duration +
                    ", aspectRatio=" + aspectRatio +
                    ", error=" + error +
                    '}';
        }
    }

    interface OnWindowStateChangedListener {
        void onWindowStateChanged(IVideoPlayer player, WindowState windowState);
    }

    interface OnPlayerStateChangedListener {
        void onPlayerStateChanged(IVideoPlayer player, PlayerState playerState);
        void onBufferingStateChanged(IVideoPlayer player, BufferingState bufferingState);
        void onVideoInfoReceived(IVideoPlayer player, boolean isStreaming, int duration);
        void onPlayPositionChanged(IVideoPlayer player, int playPositionInMillis, int durationInMillis);
        void onPlayPositionChangeRequested(IVideoPlayer player, int newPlayPositionInMillis, int durationInMillis);
        void onVolumeChanged(IVideoPlayer videoPlayer, int volumeCurrentLevel, int volumeMaxLevel);
        void onAspectRatioChanged(IVideoPlayer videoPlayer, AspectRatio aspectRatio);
        void onError(IVideoPlayer player, VideoPlayerException ex);
    }

    PlayerSnapShot takeSnapShot();
    void restore(PlayerSnapShot snapshot);

    WindowState getWindowState();
    PlayerState getTargetPlayerState();
    PlayerState getCurrentPlayerState();
    Uri getStreamAddress();
    int getPlayPosition();
    int getDuration();
    AspectRatio getAspectRatio();
    VideoPlayerException getError();
    boolean isLiveStreaming();
    int getVolumeMaxLevel();
    int getVolumeCurrentLevel();

    void changeStreamAddress(Uri streamAddress);
    void changeStreamAddress(Uri streamAddress, boolean forceIsStreaming);
    void play();
    void pause();
    void stop();
    void setPlayPosition(int milliseconds);
    void setAspectRatio(AspectRatio aspectRatio);
    void setVolumeCurrentLevel(int volume);

    void setBackgroundColor(int color);
    int getBackgroundColor();

    void registerOnWindowStateChangedListener(OnWindowStateChangedListener onWindowStateChangedListener);
    void registerOnPlayerStateChangedListener(OnPlayerStateChangedListener onPlayerStateChangedListener);
    void unregisterOnWindowStateChangedListener(OnWindowStateChangedListener onWindowStateChangedListener);
    void unregisterOnPlayerStateChangedListener(OnPlayerStateChangedListener onPlayerStateChangedListener);

}
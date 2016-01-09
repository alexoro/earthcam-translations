package com.uas.videoplayer.interfaces;

import android.os.Bundle;

/**
 * Created by a.sorokin@vectordigital.ru on 05.02.2015.
 *
 * @author a.sorokin@vectordigital.ru
 */
public class VideoPlayerException extends Exception {

    private static final long serialVersionUID = -2244368178403471316L;

    private Bundle mPlayerImplMessages;


    public VideoPlayerException(Bundle playerImplMessages) {
        this("Player internal impl error: " + playerImplMessages.toString(), playerImplMessages);
    }

    public VideoPlayerException(String detailMessage, Bundle playerImplMessages) {
        super(detailMessage);
        mPlayerImplMessages = new Bundle(playerImplMessages);
    }

    public VideoPlayerException(Throwable reason) {
        this(null, reason);
    }

    public VideoPlayerException(String detailMessage, Throwable reason) {
        super(detailMessage, reason);
    }

    public Bundle getPlayerImplMessages() {
        return mPlayerImplMessages;
    }

    @Override
    public String toString() {
        return "VideoPlayerException{" +
                "mPlayerImplMessages=" + mPlayerImplMessages +
                ", reason=" + (getCause() == null ? null : getCause().toString()) +
                '}';
    }

}
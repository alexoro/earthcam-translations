package com.uas.translations.events;

import com.uas.translations.models.CameraInfo;

/**
 * Created by UAS on 09.01.2016.
 */
public class OnTranslationRequestedEvent {

    private final CameraInfo mCameraInfo;

    public OnTranslationRequestedEvent(CameraInfo cameraInfo) {
        mCameraInfo = cameraInfo;
    }

    public CameraInfo getCameraInfo() {
        return mCameraInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnTranslationRequestedEvent that = (OnTranslationRequestedEvent) o;
        return mCameraInfo.equals(that.mCameraInfo);
    }

    @Override
    public int hashCode() {
        return mCameraInfo.hashCode();
    }

    @Override
    public String toString() {
        return "OnTranslationRequestedEvent{" +
                "mCameraInfo=" + mCameraInfo +
                '}';
    }

}
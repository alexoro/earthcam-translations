package com.uas.translations.models;

import java.io.Serializable;

/**
 * Created by UAS on 09.01.2016.
 */
public class CameraInfo implements Serializable {

    private String mName;
    private String mLink;
    private String mImageUrl;

    public CameraInfo() {

    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getLink() {
        return mLink;
    }

    public void setLink(String link) {
        mLink = link;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CameraInfo that = (CameraInfo) o;

        if (!mName.equals(that.mName)) return false;
        if (!mLink.equals(that.mLink)) return false;
        return mImageUrl.equals(that.mImageUrl);
    }

    @Override
    public int hashCode() {
        int result = mName.hashCode();
        result = 31 * result + mLink.hashCode();
        result = 31 * result + mImageUrl.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CameraInfo{" +
                "mName='" + mName + '\'' +
                ", mLink='" + mLink + '\'' +
                ", mImageUrl='" + mImageUrl + '\'' +
                '}';
    }

}
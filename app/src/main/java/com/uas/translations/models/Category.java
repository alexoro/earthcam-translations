package com.uas.translations.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by UAS on 09.01.2016.
 */
public class Category implements Serializable {

    private String mId;
    private String mName;
    private List<CameraInfo> mCameraInfoList;

    public Category() {

    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public List<CameraInfo> getCameraInfoList() {
        return mCameraInfoList;
    }

    public void setCameraInfoList(List<CameraInfo> cameraInfoList) {
        mCameraInfoList = cameraInfoList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        if (!mId.equals(category.mId)) return false;
        if (!mName.equals(category.mName)) return false;
        return mCameraInfoList.equals(category.mCameraInfoList);
    }

    @Override
    public int hashCode() {
        int result = mId.hashCode();
        result = 31 * result + mName.hashCode();
        result = 31 * result + mCameraInfoList.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Category{" +
                "mId='" + mId + '\'' +
                ", mName='" + mName + '\'' +
                ", mCameraInfoList=" + mCameraInfoList +
                '}';
    }

}
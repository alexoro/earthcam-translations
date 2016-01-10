package com.uas.translations.tablet;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.translations.models.CameraInfo;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by UAS on 09.01.2016.
 */
public class CamerasAdapter extends RecyclerView.Adapter<CamerasViewHolder> {

    private ImageLoader mImageLoader;
    private EventBus mEventBus;
    private List<CameraInfo> mCameraInfoList;

    public CamerasAdapter(ImageLoader imageLoader, EventBus eventBus, List<CameraInfo> cameraInfoList) {
        mImageLoader = imageLoader;
        mEventBus = eventBus;
        mCameraInfoList = cameraInfoList;
    }

    public void setCameraInfoList(List<CameraInfo> cameraInfoList) {
        mCameraInfoList = cameraInfoList;
    }

    @Override
    public int getItemCount() {
        return mCameraInfoList.size();
    }

    @Override
    public CamerasViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return CamerasViewHolder.newInstance(parent, mImageLoader, mEventBus);
    }

    @Override
    public void onBindViewHolder(CamerasViewHolder holder, int position) {
        holder.bind(position, mCameraInfoList);
    }

}
package com.uas.translations.phone;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.translations.models.CameraInfo;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by UAS on 09.01.2016.
 */
public class CameraInfoAdapter extends RecyclerView.Adapter<CameraInfoViewHolder> {

    private ImageLoader mImageLoader;
    private EventBus mEventBus;
    private List<CameraInfo> mCameraInfoList;

    public CameraInfoAdapter(ImageLoader imageLoader, EventBus eventBus, List<CameraInfo> cameraInfoList) {
        mImageLoader = imageLoader;
        mEventBus = eventBus;
        mCameraInfoList = cameraInfoList;
    }

    @Override
    public int getItemCount() {
        return mCameraInfoList.size();
    }

    @Override
    public CameraInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return CameraInfoViewHolder.newInstance(parent, mImageLoader, mEventBus, mCameraInfoList);
    }

    @Override
    public void onBindViewHolder(CameraInfoViewHolder holder, int position) {
        holder.bind(position);
    }

}
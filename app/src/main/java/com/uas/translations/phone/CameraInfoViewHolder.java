package com.uas.translations.phone;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.uas.translations.R;
import com.uas.translations.events.OnTranslationRequestedEvent;
import com.uas.translations.models.CameraInfo;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by UAS on 09.01.2016.
 */
public class CameraInfoViewHolder extends RecyclerView.ViewHolder {

    public static CameraInfoViewHolder newInstance(ViewGroup parent,
                                                   ImageLoader imageLoader,
                                                   EventBus eventBus,
                                                   List<CameraInfo> cameraInfoList) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.phone_camera_entry, parent, false);
        return new CameraInfoViewHolder(view, imageLoader, eventBus, cameraInfoList);
    }


    private ImageLoader mImageLoader;
    private EventBus mEventBus;
    private List<CameraInfo> mCameraInfoList;
    private int mPosition;
    private ImageView mIcon;
    private TextView mLabel;
    private ImageViewAware mImageViewAware;
    private DisplayImageOptions mDisplayImageOptions;


    public CameraInfoViewHolder(View itemView,
                                ImageLoader imageLoader,
                                EventBus eventBus,
                                List<CameraInfo> cameraInfoList) {
        super(itemView);

        mImageLoader = imageLoader;
        mCameraInfoList = cameraInfoList;
        mEventBus = eventBus;
        mPosition = -1;

        mIcon = (ImageView) itemView.findViewById(R.id.icon);
        mLabel = (TextView) itemView.findViewById(R.id.label);
        mImageViewAware = new ImageViewAware(mIcon);
        mDisplayImageOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.thumb)
                .showImageForEmptyUri(R.drawable.thumb)
                .resetViewBeforeLoading(true)
                .cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new SimpleBitmapDisplayer())
                .build();
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPosition >= 0) {
                    CameraInfo cameraInfo = mCameraInfoList.get(mPosition);
                    OnTranslationRequestedEvent event = new OnTranslationRequestedEvent(cameraInfo);
                    mEventBus.post(event);
                }
            }
        });
    }

    public void bind(int position) {
        mPosition = position;
        CameraInfo cameraInfo = mCameraInfoList.get(position);
        mLabel.setText(cameraInfo.getName());
        mImageLoader.displayImage(cameraInfo.getImageUrl(), mImageViewAware, mDisplayImageOptions);
    }

}
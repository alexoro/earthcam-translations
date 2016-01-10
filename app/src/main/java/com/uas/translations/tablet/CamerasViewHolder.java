package com.uas.translations.tablet;

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
public class CamerasViewHolder extends RecyclerView.ViewHolder {

    public static CamerasViewHolder newInstance(ViewGroup parent,
                                                ImageLoader imageLoader,
                                                EventBus eventBus) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.tablet_cameras_entry, parent, false);
        return new CamerasViewHolder(view, imageLoader, eventBus);
    }


    private ImageLoader mImageLoader;
    private EventBus mEventBus;
    private CameraInfo mCameraInfo;
    private ImageView mIcon;
    private TextView mLabel;
    private ImageViewAware mImageViewAware;
    private DisplayImageOptions mDisplayImageOptions;


    public CamerasViewHolder(View itemView,
                             ImageLoader imageLoader,
                             EventBus eventBus) {
        super(itemView);

        mImageLoader = imageLoader;
        mEventBus = eventBus;
        mCameraInfo = null;

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
                if (mCameraInfo != null) {
                    OnTranslationRequestedEvent event = new OnTranslationRequestedEvent(mCameraInfo);
                    mEventBus.post(event);
                }
            }
        });
    }

    public void bind(int position, List<CameraInfo> cameraInfoList) {
        mCameraInfo = cameraInfoList.get(position);
        mLabel.setText(mCameraInfo.getName());
        mImageLoader.displayImage(mCameraInfo.getImageUrl(), mImageViewAware, mDisplayImageOptions);
    }

}
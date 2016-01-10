package com.uas.translations.tablet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.translations.EventBusProvider;
import com.uas.translations.R;
import com.uas.translations.TranslationsProvider;
import com.uas.translations.events.OnCategoryRequestedEvent;
import com.uas.translations.models.CameraInfo;
import com.uas.translations.models.Category;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by UAS on 10.01.2016.
 */
public class CamerasFragment extends Fragment {

    public static CamerasFragment newInstance() {
        return new CamerasFragment();
    }


    private static class ViewHolder {
        public RecyclerView list;
        public CamerasAdapter adapter;
    }

    private ImageLoader mImageLoader;
    private EventBus mEventBus;
    private TranslationsProvider mTranslationsProvider;
    private ViewHolder mViewHolder;
    private Category mCategory;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageLoader = ImageLoader.getInstance();
        mEventBus = EventBusProvider.getInstance();
        mTranslationsProvider = TranslationsProvider.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tablet_cameras, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewHolder = new ViewHolder();
        mViewHolder.list = (RecyclerView) view.findViewById(R.id.list);

        /*LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mViewHolder.list.setLayoutManager(linearLayoutManager);*/
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
        mViewHolder.list.setLayoutManager(gridLayoutManager);
        mViewHolder.adapter = new CamerasAdapter(mImageLoader, mEventBus, new ArrayList<CameraInfo>(0));
        mViewHolder.list.setAdapter(mViewHolder.adapter);
        mViewHolder.list.setHasFixedSize(true);

        mEventBus.register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mEventBus.unregister(this);
        mViewHolder = null;
    }

    public void onEventMainThread(OnCategoryRequestedEvent event) {
        mCategory = event.getCategory();
        mViewHolder.adapter.setCameraInfoList(mCategory.getCameraInfoList());
        mViewHolder.adapter.notifyDataSetChanged();
    }

}
package com.uas.translations.phone;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.uas.translations.EventBusProvider;
import com.uas.translations.R;
import com.uas.translations.TranslationsProvider;
import com.uas.translations.models.Category;
import com.uas.translations.utils.RecyclerViewUilPauseOnScrollListener;

import de.greenrobot.event.EventBus;

/**
 * Created by UAS on 09.01.2016.
 */
public class PhoneTranslationListFragment extends Fragment {

    private static final String ARGS_CATEGORY = "category";

    public static PhoneTranslationListFragment newInstance(Category category) {
        Bundle args = new Bundle();
        args.putSerializable(ARGS_CATEGORY, category);
        PhoneTranslationListFragment fragment = new PhoneTranslationListFragment();
        fragment.setArguments(args);
        return fragment;
    }


    private static class ViewHolder {
        public RecyclerView list;
        public CameraInfoAdapter adapter;
        public RecyclerViewUilPauseOnScrollListener uilPauseOnRecyclerScrollListener;
    }

    private ImageLoader mImageLoader;
    private EventBus mEventBus;
    private TranslationsProvider mTranslationsProvider;
    private Category mCategory;
    private ViewHolder mViewHolder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageLoader = ImageLoader.getInstance();
        mEventBus = EventBusProvider.getInstance();
        mTranslationsProvider = TranslationsProvider.getInstance();

        mCategory = (Category) getArguments().getSerializable(ARGS_CATEGORY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.phone_category, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewHolder = new ViewHolder();
        mViewHolder.list = (RecyclerView) view.findViewById(R.id.list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mViewHolder.list.setLayoutManager(linearLayoutManager);
        mViewHolder.adapter = new CameraInfoAdapter(mImageLoader, mEventBus, mCategory.getCameraInfoList());
        mViewHolder.list.setAdapter(mViewHolder.adapter);

        mViewHolder.uilPauseOnRecyclerScrollListener = new RecyclerViewUilPauseOnScrollListener(
                mImageLoader,
                true);
        mViewHolder.list.addOnScrollListener(mViewHolder.uilPauseOnRecyclerScrollListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewHolder.list.removeOnScrollListener(mViewHolder.uilPauseOnRecyclerScrollListener);
        mViewHolder = null;
    }

}
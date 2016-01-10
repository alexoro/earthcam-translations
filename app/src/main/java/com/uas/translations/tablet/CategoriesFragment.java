package com.uas.translations.tablet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.uas.translations.EventBusProvider;
import com.uas.translations.R;
import com.uas.translations.TranslationsProvider;

import de.greenrobot.event.EventBus;

/**
 * Created by UAS on 10.01.2016.
 */
public class CategoriesFragment extends Fragment {

    public static CategoriesFragment newInstance() {
        return new CategoriesFragment();
    }


    private static class ViewHolder {
        public RecyclerView list;
        public CategoriesAdapter adapter;
    }

    private EventBus mEventBus;
    private TranslationsProvider mTranslationsProvider;
    private ViewHolder mViewHolder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventBus = EventBusProvider.getInstance();
        mTranslationsProvider = TranslationsProvider.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tablet_category, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewHolder = new ViewHolder();
        mViewHolder.list = (RecyclerView) view.findViewById(R.id.list);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mViewHolder.list.setLayoutManager(linearLayoutManager);
        mViewHolder.adapter = new CategoriesAdapter(mEventBus, mTranslationsProvider.getCategories());
        mViewHolder.list.setAdapter(mViewHolder.adapter);
        mViewHolder.list.setHasFixedSize(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewHolder = null;
    }

}
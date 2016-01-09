package com.uas.translations.phone;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.uas.translations.R;
import com.uas.translations.TranslationsProvider;

/**
 * Created by UAS on 09.01.2016.
 */
public class PhoneMainFragment extends Fragment {

    public static PhoneMainFragment newInstance() {
        return new PhoneMainFragment();
    }


    private static class ViewHolder {
        public Toolbar toolbar;
        public SmartTabLayout tabs;
        public TabsViewProvider tabsViewProvider;
        public ViewPager contentViewPager;
    }


    private TranslationsProvider mTranslationsProvider;
    private ViewHolder mViewHolder;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTranslationsProvider = TranslationsProvider.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.phone_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mViewHolder = new ViewHolder();
        mViewHolder.tabs = (SmartTabLayout) view.findViewById(R.id.tabs);
        mViewHolder.contentViewPager = (ViewPager) view.findViewById(R.id.content);

        TabsAdapter adapter = new TabsAdapter(getChildFragmentManager(), mTranslationsProvider);
        mViewHolder.contentViewPager.setAdapter(adapter);
        mViewHolder.contentViewPager.setOffscreenPageLimit(2);

        mViewHolder.tabsViewProvider = new TabsViewProvider(mTranslationsProvider);
        mViewHolder.tabs.setCustomTabView(mViewHolder.tabsViewProvider);
        mViewHolder.tabs.setViewPager(mViewHolder.contentViewPager);
        mViewHolder.tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                PhoneMainFragment.this.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mViewHolder.contentViewPager.setCurrentItem(mTranslationsProvider.getCategories().size() / 2, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mViewHolder = null;
    }

    protected void onPageSelected(int page) {
//        switch (page) {
//            case TabsPages.CONTACTS:
//                mModuleConfig.getEventBus().post(new OnFriendsListOpenedEvent(this));
//                break;
//            case TabsPages.MAIN_DIALOGS:
//            case TabsPages.MUTED_DIALOGS:
//            default:
//                mModuleConfig.getEventBus().post(new OnDialogsListOpenedEvent(this));
//                break;
//        }
    }

}
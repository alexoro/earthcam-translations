package com.uas.translations.phone;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.uas.translations.TranslationsProvider;

/**
 * Created by UAS on 09.01.2016.
 */
public class TabsAdapter extends FragmentStatePagerAdapter {

    private TranslationsProvider mTranslationsProvider;

    public TabsAdapter(FragmentManager fm, TranslationsProvider translationsProvider) {
        super(fm);
        mTranslationsProvider = translationsProvider;
    }

    @Override
    public Fragment getItem(int position) {
        return PhoneTranslationListFragment.newInstance(mTranslationsProvider.getCategories().get(position));
    }

    @Override
    public int getCount() {
        return mTranslationsProvider.getCategories().size();
    }

}
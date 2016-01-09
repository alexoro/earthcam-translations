package com.uas.translations.phone;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.uas.translations.R;
import com.uas.translations.TranslationsProvider;
import com.uas.translations.models.Category;

/**
 * Created by UAS on 09.01.2016.
 */
public class TabsViewProvider implements SmartTabLayout.TabProvider {

    private TranslationsProvider mTranslationsProvider;

    public TabsViewProvider(TranslationsProvider translationsProvider) {
        mTranslationsProvider = translationsProvider;
    }

    @Override
    public View createTabView(ViewGroup tabStrip, int position, PagerAdapter pagerAdapter) {
        // required to make that ^&**% library work
        LinearLayout customWrapper = new LinearLayout(tabStrip.getContext());
        customWrapper.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1));

        View custom = LayoutInflater.from(tabStrip.getContext()).inflate(
                R.layout.phone_tab,
                customWrapper,
                false);
        customWrapper.addView(custom);

        onBindCustomTabView(custom, position);
        return customWrapper;
    }

    protected void onBindCustomTabView(View view, int position) {
        TextView label = (TextView) view.findViewById(R.id.label);
        Category category = mTranslationsProvider.getCategories().get(position);
        label.setText(category.getName());
    }

}
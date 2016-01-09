package com.uas.translations.phone;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.ogaclejapan.smarttablayout.SmartTabLayout;

/**
 * Created by UAS on 09.01.2016.
 */
public class TabLayoutWithInvalidate extends SmartTabLayout {

    private ViewPager mViewPager;

    public TabLayoutWithInvalidate(Context context) {
        super(context);
    }

    public TabLayoutWithInvalidate(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TabLayoutWithInvalidate(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setViewPager(ViewPager viewPager) {
        mViewPager = viewPager;
        super.setViewPager(viewPager);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        super.setViewPager(mViewPager);
    }

    @Override
    public void invalidate(int l, int t, int r, int b) {
        super.invalidate(l, t, r, b);
        super.setViewPager(mViewPager);
    }

    @Override
    public void invalidate(@NonNull Rect dirty) {
        super.invalidate(dirty);
        super.setViewPager(mViewPager);
    }

}
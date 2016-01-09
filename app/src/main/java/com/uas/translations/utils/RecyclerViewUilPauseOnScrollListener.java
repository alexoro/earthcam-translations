package com.uas.translations.utils;

import android.support.v7.widget.RecyclerView;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by UAS on 09.01.2016.
 */
public class RecyclerViewUilPauseOnScrollListener extends RecyclerView.OnScrollListener {

    private ImageLoader mImageLoader;
    private boolean mPauseOnScroll;


    /**
     * Constructor
     *
     * @param imageLoader   {@linkplain ImageLoader} instance for controlling
     * @param pauseOnScroll Whether {@linkplain ImageLoader#pause() pause ImageLoader} during touch scrolling
     */
    public RecyclerViewUilPauseOnScrollListener(ImageLoader imageLoader, boolean pauseOnScroll) {
        mImageLoader = imageLoader;
        mPauseOnScroll = pauseOnScroll;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        switch (newState) {
            case RecyclerView.SCROLL_STATE_IDLE:
                mImageLoader.resume();
                break;
            case RecyclerView.SCROLL_STATE_DRAGGING:
                if (mPauseOnScroll) {
                    mImageLoader.pause();
                }
                break;
            case RecyclerView.SCROLL_STATE_SETTLING:
                mImageLoader.resume();
                break;
        }
    }

}
package com.uas.videoplayer.mediaplayerimpl;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by a.sorokin@vectordigital.ru on 26.02.2015.
 *
 * @author a.sorokin@vectordigital.ru
 */
public class FrameLayoutWithSizeChangeListener extends FrameLayout {

    public interface OnSizeChangedListener {
        void onSizeChanged(int oldWidth, int oldHeight, int newWidth, int newHeight);
    }

    private OnSizeChangedListener mOnSizeChangedListener;

    public FrameLayoutWithSizeChangeListener(Context context) {
        super(context);
    }

    public FrameLayoutWithSizeChangeListener(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FrameLayoutWithSizeChangeListener(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public FrameLayoutWithSizeChangeListener(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnSizeChangedListener(OnSizeChangedListener onSizeChangedListener) {
        mOnSizeChangedListener = onSizeChangedListener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mOnSizeChangedListener != null) {
            mOnSizeChangedListener.onSizeChanged(oldw, oldh, w, h);
        }
    }

}
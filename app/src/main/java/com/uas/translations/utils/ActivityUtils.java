package com.uas.translations.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;

/**
 * Created by UAS on 09.01.2016.
 */
public class ActivityUtils {

    public static boolean isAlive(Activity activity) {
        return activity != null
                && !activity.isFinishing()
                && (Build.VERSION.SDK_INT < 17 ? true : !isActivityDestroyed(activity));
    }

    @TargetApi(17)
    private static boolean isActivityDestroyed(Activity activity) {
        return activity.isDestroyed();
    }

    public static void removeActivityBackgroundCompat(Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity is null");
        }

        if (Build.VERSION.SDK_INT >= 16) {
            activity.getWindow().getDecorView().setBackground(null);
        } else {
            activity.getWindow().getDecorView().setBackgroundDrawable(null);
        }
    }

}
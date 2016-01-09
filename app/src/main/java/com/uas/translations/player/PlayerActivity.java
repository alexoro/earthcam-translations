package com.uas.translations.player;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.uas.translations.models.CameraInfo;

/**
 * Created by UAS on 09.01.2016.
 */
public class PlayerActivity extends AppCompatActivity {

    public static final String EXTRA_CAMERA_INFO = "camera_info";

    public static Intent createLaunchIntent(Context context, CameraInfo cameraInfo) {
        Intent intent = new Intent(context, PlayerActivity.class);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.putExtra(EXTRA_CAMERA_INFO, cameraInfo);
        return intent;
    }

}
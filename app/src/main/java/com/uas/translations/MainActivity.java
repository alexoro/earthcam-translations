package com.uas.translations;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.uas.translations.events.OnTranslationRequestedEvent;
import com.uas.translations.phone.PhoneMainFragment;
import com.uas.translations.player.PlayerActivity;
import com.uas.translations.tablet.CamerasFragment;
import com.uas.translations.tablet.CategoriesFragment;
import com.uas.translations.utils.ActivityUtils;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {

    private EventBus mEventBus;
    private boolean mIsMobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityUtils.removeActivityBackgroundCompat(this);
        setContentView(R.layout.activity_main);

        mEventBus = EventBusProvider.getInstance();
        mIsMobile = getResources().getBoolean(R.bool.is_mobile);

        if (mIsMobile) {
            PhoneMainFragment fragment = PhoneMainFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
        } else {
            CategoriesFragment categoriesFragment = CategoriesFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.categories, categoriesFragment)
                    .commit();
            CamerasFragment camerasFragment = CamerasFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.cameras, camerasFragment)
                    .commit();
        }

        mEventBus.register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEventBus.unregister(this);
    }

    public void onEventMainThread(OnTranslationRequestedEvent event) {
        Intent intent = PlayerActivity.createLaunchIntent(this, event.getCameraInfo());
        startActivity(intent);
    }

}

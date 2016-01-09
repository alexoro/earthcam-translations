package com.uas.translations;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.L;

/**
 * Created by UAS on 09.01.2016.
 */
public class CustomApplication extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;

        L.writeDebugLogs(false);
        L.writeLogs(false);
        L.disableLogging();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(sContext)
                .threadPoolSize(3)
                .tasksProcessingOrder(QueueProcessingType.FIFO)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(8 * 1024 * 1024))
                .imageDownloader(new BaseImageDownloader(sContext))
                .imageDecoder(new BaseImageDecoder(false))
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);
    }

    public static Context getContext() {
        return sContext;
    }

}
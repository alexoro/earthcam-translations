package com.uas.translations;

import android.content.Context;

import com.uas.translations.models.CameraInfo;
import com.uas.translations.models.Category;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by UAS on 09.01.2016.
 */
public class TranslationsProvider {

    private static class SingletonHolder {
        public static final TranslationsProvider INSTANCE = new TranslationsProvider(CustomApplication.getContext());
    }

    public static TranslationsProvider getInstance()  {
        return SingletonHolder.INSTANCE;
    }


    private List<Category> mCategories;

    private TranslationsProvider(Context context) {
        parse(context);
    }

    public List<Category> getCategories() {
        return mCategories;
    }

    private void parse(Context context) {
        try {
            InputStream is = context.getAssets().open("data.json");

            StringBuilder sb = new StringBuilder();
            int ch;
            while((ch = is.read()) != -1){
                sb.append((char) ch);
            }
            String jsonString = sb.toString();

            mCategories = new ArrayList<>();

            JSONArray jaCategories = new JSONArray(jsonString);
            for (int i = 0; i < jaCategories.length(); i++) {
                JSONObject joCategory = jaCategories.getJSONObject(i);
                Category category = new Category();
                category.setId(joCategory.getString("id"));
                category.setName(joCategory.getString("name"));
                category.setCameraInfoList(new ArrayList<CameraInfo>());
                JSONArray jaCameras = joCategory.getJSONArray("items");
                for (int j = 0; j < jaCameras.length(); j++) {
                    JSONObject joCamera = jaCameras.getJSONObject(j);
                    CameraInfo cameraInfo = new CameraInfo();
                    cameraInfo.setName(joCamera.getString("name"));
                    cameraInfo.setLink(joCamera.getString("link"));
                    cameraInfo.setImageUrl("assets://" + joCamera.getString("image"));
                    category.getCameraInfoList().add(cameraInfo);
                }
                mCategories.add(category);
            }

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
package com.uas.translations.tablet;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.uas.translations.models.Category;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by UAS on 09.01.2016.
 */
public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesViewHolder> {

    private EventBus mEventBus;
    private List<Category> mCategoryList;

    public CategoriesAdapter(EventBus eventBus, List<Category> categoryList) {
        mEventBus = eventBus;
        mCategoryList = categoryList;
    }

    public void setCategoryList(List<Category> categoryList) {
        mCategoryList = categoryList;
    }

    @Override
    public int getItemCount() {
        return mCategoryList.size();
    }

    @Override
    public CategoriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return CategoriesViewHolder.newInstance(parent, mEventBus, mCategoryList);
    }

    @Override
    public void onBindViewHolder(CategoriesViewHolder holder, int position) {
        holder.bind(position);
    }

}
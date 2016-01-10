package com.uas.translations.tablet;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.uas.translations.R;
import com.uas.translations.events.OnCategoryRequestedEvent;
import com.uas.translations.models.Category;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by UAS on 09.01.2016.
 */
public class CategoriesViewHolder extends RecyclerView.ViewHolder {

    public static CategoriesViewHolder newInstance(ViewGroup parent,
                                                   EventBus eventBus,
                                                   List<Category> categoryList) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.tablet_category_entry, parent, false);
        return new CategoriesViewHolder(view, eventBus, categoryList);
    }


    private EventBus mEventBus;
    private List<Category> mCategoryList;
    private int mPosition;
    private TextView mLabel;


    public CategoriesViewHolder(View itemView,
                                EventBus eventBus,
                                List<Category> categoryList) {
        super(itemView);

        mEventBus = eventBus;
        mCategoryList = categoryList;
        mPosition = -1;

        mLabel = (TextView) itemView.findViewById(R.id.label);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPosition >= 0) {
                    Category category = mCategoryList.get(mPosition);
                    OnCategoryRequestedEvent event = new OnCategoryRequestedEvent(category);
                    mEventBus.post(event);
                }
            }
        });
    }

    public void bind(int position) {
        mPosition = position;
        Category category = mCategoryList.get(mPosition);
        mLabel.setText(category.getName());
    }

}
package com.uas.translations.events;

import com.uas.translations.models.Category;

/**
 * Created by UAS on 10.01.2016.
 */
public class OnCategoryRequestedEvent {

    private final Category mCategory;

    public OnCategoryRequestedEvent(Category category) {
        mCategory = category;
    }

    public Category getCategory() {
        return mCategory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OnCategoryRequestedEvent that = (OnCategoryRequestedEvent) o;
        return mCategory.equals(that.mCategory);
    }

    @Override
    public int hashCode() {
        return mCategory.hashCode();
    }

    @Override
    public String toString() {
        return "OnCategoryRequestedEvent{" +
                "mCategory=" + mCategory +
                '}';
    }

}
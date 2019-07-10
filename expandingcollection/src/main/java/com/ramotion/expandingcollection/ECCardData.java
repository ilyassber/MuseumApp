package com.ramotion.expandingcollection;


import android.graphics.Bitmap;

import java.util.List;

import androidx.annotation.DrawableRes;

/**
 * Implement this interface to provide data to pager view and content list inside pager card
 *
 * @param <T> Type of items in card content list
 */
public interface ECCardData<T> {

    Integer getMuseumId();

    String getCardTitle();

    String getDescription();

    String getCountry();

    String getCity();

    Bitmap  getMainBackgroundResourceBitmap();

    Bitmap  getHeadBackgroundResourceBitmap();

    Integer getMainBitmapKey();

    @DrawableRes
    Integer getMainBackgroundResource();

    @DrawableRes
    Integer getHeadBackgroundResource();

    List<T> getListItems();
}

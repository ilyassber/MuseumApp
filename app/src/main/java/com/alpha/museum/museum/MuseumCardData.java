package com.alpha.museum.museum;

import android.graphics.Bitmap;

import com.ramotion.expandingcollection.ECCardData;

import java.util.List;

public class MuseumCardData implements ECCardData<String> {

    private String          cardTitle;
    private String          description;
    private String          country;
    private String          city;
    private Integer         mainBackgroundRes;
    private Integer         headBackgroundRes;
    private Bitmap          mainBitmapRes;
    private Bitmap          headBitmapRes;
    private Integer         mainBitmapKey;
    private List<String>    listItems;

    public MuseumCardData(String cardTitle, String description, String country, String city, Integer mainBackgroundRes, Integer headBackgroundRes, Bitmap mainBitmapRes, Bitmap headBitmapRes, Integer mainBitmapKey)
    {
        this.cardTitle = cardTitle;
        this.description = description;
        this.country = country;
        this.city = city;
        this.mainBackgroundRes = mainBackgroundRes;
        this.headBackgroundRes = headBackgroundRes;
        this.mainBitmapRes = mainBitmapRes;
        this.headBitmapRes = headBitmapRes;
        this.mainBitmapKey = mainBitmapKey;
    }

    @Override
    public Integer getMuseumId() {
        return null;
    }

    public String getCardTitle() {
        return cardTitle;
    }

    public String getDescription() {
        return description;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    @Override
    public Bitmap getMainBackgroundResourceBitmap() {
        return mainBitmapRes;
    }

    @Override
    public Bitmap getHeadBackgroundResourceBitmap() {
        return headBitmapRes;
    }

    @Override
    public Integer getMainBitmapKey() {
        return mainBitmapKey;
    }

    @Override
    public Integer getMainBackgroundResource() {
        return mainBackgroundRes;
    }

    @Override
    public Integer getHeadBackgroundResource() {
        return headBackgroundRes;
    }

    @Override
    public List<String> getListItems() {
        return null;
    }
}
package com.alpha.museum.museum.models;

import android.graphics.Bitmap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.ramotion.expandingcollection.ECCardData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Museum implements ECCardData<String>, Serializable {

    @SerializedName("museum_id")
    @Expose
    private Integer museumId;
    @SerializedName("museum_name")
    @Expose
    private String museumName;
    @SerializedName("museum_country")
    @Expose
    private String museumCountry;
    @SerializedName("museum_city")
    @Expose
    private String museumCity;
    @SerializedName("gps_location")
    @Expose
    private String gpsLocation;
    @SerializedName("museum_description")
    @Expose
    private String museumDescription;

    // list of images
    private ArrayList<Image> images;
    // list of videos
    private ArrayList<Video> videos;

    @Override
    public Integer getMuseumId() {
        return museumId;
    }

    public void setMuseumId(Integer museumId) {
        this.museumId = museumId;
    }

    public String getMuseumName() {
        return museumName;
    }

    public void setMuseumName(String museumName) {
        this.museumName = museumName;
    }

    public String getMuseumCountry() {
        return museumCountry;
    }

    public void setMuseumCountry(String museumCountry) {
        this.museumCountry = museumCountry;
    }

    public String getMuseumCity() {
        return museumCity;
    }

    public void setMuseumCity(String museumCity) {
        this.museumCity = museumCity;
    }

    public String getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public String getMuseumDescription() {
        return museumDescription;
    }

    public void setMuseumDescription(String museumDescription) {
        this.museumDescription = museumDescription;
    }

    public ArrayList<Image> getImages() {
        return images;
    }

    public void setImages(ArrayList<Image> images) {
        this.images = images;
    }

    public ArrayList<Video> getVideos() {
        return videos;
    }

    public void setVideos(ArrayList<Video> videos) {
        this.videos = videos;
    }

    @Override
    public String getCardTitle() {
        return getMuseumName();
    }

    @Override
    public String getDescription() {
        return getMuseumDescription();
    }

    @Override
    public String getCountry() {
        return getMuseumCountry();
    }

    @Override
    public String getCity() {
        return getMuseumCity();
    }

    @Override
    public Bitmap getMainBackgroundResourceBitmap() {
        return getImages().get(0).getImgBitmap();
    }

    @Override
    public Bitmap getHeadBackgroundResourceBitmap() {
        return getImages().get(0).getImgBitmap();
    }

    @Override
    public Integer getMainBitmapKey() {
        return getImages().get(0).getImgId();
    }

    @Override
    public Integer getMainBackgroundResource() {
        return null;
    }

    @Override
    public Integer getHeadBackgroundResource() {
        return null;
    }

    @Override
    public List<String> getListItems() {
        return null;
    }
}
package com.alpha.museum.museum.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class Monument implements Serializable {

    @SerializedName("monument_id")
    @Expose
    private Integer monumentId;
    @SerializedName("monument_title")
    @Expose
    private String monumentTitle;
    @SerializedName("monument_description")
    @Expose
    private String monumentDescription;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;
    @SerializedName("categories")
    @Expose
    private ArrayList<Category> categories = new ArrayList<>();
    @SerializedName("images")
    @Expose
    private ArrayList<Image> images = new ArrayList<>();
    @SerializedName("videos")
    @Expose
    private ArrayList<Video> videos = new ArrayList<>();

    public Integer getMonumentId() {
        return monumentId;
    }

    public void setMonumentId(Integer monumentId) {
        this.monumentId = monumentId;
    }

    public String getMonumentTitle() {
        return monumentTitle;
    }

    public void setMonumentTitle(String monumentTitle) {
        this.monumentTitle = monumentTitle;
    }

    public String getMonumentDescription() {
        return monumentDescription;
    }

    public void setMonumentDescription(String monumentDescription) {
        this.monumentDescription = monumentDescription;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<Category> categories) {
        this.categories = categories;
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
}

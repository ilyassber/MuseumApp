package com.alpha.museum.museum.models;

import android.graphics.Bitmap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Image implements Serializable {

    @SerializedName("img_id")
    @Expose
    private Integer imgId;
    @SerializedName("img_title")
    @Expose
    private String imgTitle;
    @SerializedName("img_description")
    @Expose
    private String imgDescription;
    @SerializedName("img_path")
    @Expose
    private String imgPath;
    @SerializedName("is360")
    @Expose
    private Integer is360;

    private transient Bitmap imgBitmap;

    public Integer getImgId() {
        return imgId;
    }

    public void setImgId(Integer imgId) {
        this.imgId = imgId;
    }

    public String getImgTitle() {
        return imgTitle;
    }

    public void setImgTitle(String imgTitle) {
        this.imgTitle = imgTitle;
    }

    public String getImgDescription() {
        return imgDescription;
    }

    public void setImgDescription(String imgDescription) {
        this.imgDescription = imgDescription;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public Bitmap getImgBitmap() {
        return imgBitmap;
        //return getImgBitmapByteArray();
    }

    public void setImgBitmap(Bitmap imgBitmap) {
        this.imgBitmap = imgBitmap;
    }

    public Integer getIs360() {
        return is360;
    }

    public void setIs360(Integer is360) {
        this.is360 = is360;
    }
}

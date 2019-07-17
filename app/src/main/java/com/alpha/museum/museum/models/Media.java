package com.alpha.museum.museum.models;

import java.io.Serializable;

public class Media implements Serializable {

    private int type;
    private Image image;
    private Video video;
    private Image vr;

    public Media (int type, Image image, Video video, Image vr) {
        this.type = type;
        this.image = image;
        this.video = video;
        this.vr = vr;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public Video getVideo() {
        return video;
    }

    public void setVideo(Video video) {
        this.video = video;
    }

    public Image getVr() {
        return vr;
    }

    public void setVr(Image vr) {
        this.vr = vr;
    }
}

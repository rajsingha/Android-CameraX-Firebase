package com.camerax.firebase.app.model;

public class ImageData {

    private String title;
    private String imageUrl;
    private String date;

    public ImageData(){}

    public ImageData(String title, String imageUrl, String date) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

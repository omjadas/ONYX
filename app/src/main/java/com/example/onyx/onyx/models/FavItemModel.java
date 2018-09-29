package com.example.onyx.onyx.models;

/**
 * Created by Wolf Soft on 1/17/2018.
 */

public class FavItemModel {

    Integer image;
    String number,title,view,install;

    public FavItemModel(Integer image, String number, String title, String view, String install) {
        this.image = image;
        this.number = number;
        this.title = title;
        this.view = view;
        this.install = install;
    }

    public Integer getImage() {
        return image;
    }

    public void setImage(Integer image) {
        this.image = image;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getInstall() {
        return install;
    }

    public void setInstall(String install) {
        this.install = install;
    }
}

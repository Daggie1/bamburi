package com.muva.bamburi.models;

import java.util.List;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by Njoro on 4/26/18.
 */
@Entity
public class PhotoCorner {
    @Id(assignable = true)
    private long id;

    private String name;
    private int total_photos;
    private String latest_photo_url;
    private String created_at;
    private List<Photo> photos;
    private int order_index;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotal_photos() {
        return total_photos;
    }

    public void setTotal_photos(int total_photos) {
        this.total_photos = total_photos;
    }

    public String getLatest_photo_url() {
        return latest_photo_url;
    }

    public void setLatest_photo_url(String latest_photo_url) {
        this.latest_photo_url = latest_photo_url;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public int getOrder_index() {
        return order_index;
    }

    public void setOrder_index(int order_index) {
        this.order_index = order_index;
    }
}

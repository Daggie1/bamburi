package com.muva.bamburi.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by Njoro on 4/27/18.
 */
@Entity
public class Photo {
    @Id(assignable = true)
    private long id;

    private String photo_server_url;
    private String photo_local_url;
    private boolean downloaded;
    private int photo_corner_id;
    private String created_at;
    private String deleted_at;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhoto_server_url() {
        return photo_server_url;
    }

    public void setPhoto_server_url(String photo_server_url) {
        this.photo_server_url = photo_server_url;
    }

    public String getPhoto_local_url() {
        return photo_local_url;
    }

    public void setPhoto_local_url(String photo_local_url) {
        this.photo_local_url = photo_local_url;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public int getPhoto_corner_id() {
        return photo_corner_id;
    }

    public void setPhoto_corner_id(int photo_corner_id) {
        this.photo_corner_id = photo_corner_id;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(String deleted_at) {
        this.deleted_at = deleted_at;
    }
}

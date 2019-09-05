package com.muva.bamburi.models;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.utils.TimeAgo;
import com.muva.bamburi.utils.UniversalMethods;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by Njoro on 4/27/18.
 */
@Entity
public class Video {

    @Id(assignable = true)
    private long id;

    private String caption;
    private String video_url;
    private String video_thumbnail;
    private String created_at;
    private String deleted_at;
    private int order_index;

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    private boolean downloaded;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

    public String getVideo_thumbnail() {
        return video_thumbnail;
    }

    public void setVideo_thumbnail(String video_thumbnail) {
        this.video_thumbnail = video_thumbnail;
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

    public int getOrder_index() {
        return order_index;
    }

    public void setOrder_index(int order_index) {
        this.order_index = order_index;
    }

    public String getTime_created() {
        try {
            Date mDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(this.created_at);
            return TimeAgo.toRelative(new Date(mDate.getTime()), new Date(new Date().getTime()), 1);
        } catch (Exception e) {
            return "0secs";
        }
    }

    public boolean isDownloaded() {
        return UniversalMethods.fileInInternalStorage(Bamburi.getInstance()
                , getVideo_url());
    }
}

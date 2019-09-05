package com.muva.bamburi.models;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.utils.TimeAgo;
import com.muva.bamburi.utils.UniversalMethods;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

import static com.muva.bamburi.utils.Constants.AUDIO_MEDIA;
import static com.muva.bamburi.utils.Constants.VIDEO_MEDIA;

/**
 * Created by Njoro on 4/25/18.
 */
@Entity
public class News {
    @Id(assignable = true)
    private long id;

    private String title;
    private String message;
    private boolean repliable;
    public List<Comment> responses;
    private int news_type;
    private int media_type;
    private String media_url;
    private String media_size;
    private String admin_name;
    private String created_at;
    private boolean downloaded;
    private String deleted_at;
    private int order_index;
    private String pdf_url;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRepliable() {
        return repliable;
    }

    public void setRepliable(boolean repliable) {
        this.repliable = repliable;
    }


    public int getNews_type() {
        return news_type;
    }

    public void setNews_type(int news_type) {
        this.news_type = news_type;
    }

    public int getMedia_type() {
        return media_type;
    }

    public void setMedia_type(int media_type) {
        this.media_type = media_type;
    }

    public String getMedia_url() {
        return media_url;
    }

    public void setMedia_url(String media_url) {
        this.media_url = media_url;
    }

    public String getMedia_size() {
        return media_size;
    }

    public void setMedia_size(String media_size) {
        this.media_size = media_size;
    }

    public String getAdmin_name() {
        return admin_name;
    }

    public void setAdmin_name(String admin_name) {
        this.admin_name = admin_name;
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

    public boolean isDownloaded() {
        if (getMedia_type() ==  VIDEO_MEDIA || getMedia_type() == AUDIO_MEDIA) {
            return UniversalMethods.fileInInternalStorage(Bamburi.getInstance()
                    , getMedia_url());
        }

        else{
            return true;
        }
    }

    public String getPdf_url() {
        return pdf_url;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;

    }

    public String getTime_created() {
        try {
            Date mDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(this.created_at);
            return TimeAgo.toRelative(new Date(mDate.getTime()), new Date(new Date().getTime()), 1);
        } catch (Exception e) {
            return "0secs";
        }
    }

    public boolean hasComments() {
        return Bamburi.getInstance()
                .getBoxStore()
                .boxFor(Comment.class)
                .query()
                .equal(Comment_.news_id,this.id)
                .build()
                .find()
                .size() > 0;
    }

}

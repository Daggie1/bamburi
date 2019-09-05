package com.muva.bamburi.models;

import com.muva.bamburi.utils.TimeAgo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by Njoro on 5/3/18.
 */
@Entity
public class Comment {
    @Id(assignable = true)
    private long id;

    private int news_id;
    private String admin_name;
    private int from;
    private String response;
    private String created_at;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getNews_id() {
        return news_id;
    }

    public void setNews_id(int news_id) {
        this.news_id = news_id;
    }

    public String getAdmin_name() {
        return admin_name;
    }

    public void setAdmin_name(String admin_name) {
        this.admin_name = admin_name;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getTime_created() {
        try {
            Date mDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(this.created_at);
            return TimeAgo.toRelative(new Date(mDate.getTime()), new Date(new Date().getTime()), 1);
        } catch (Exception e) {
            return "0secs";
        }
    }
}

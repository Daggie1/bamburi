package com.muva.bamburi.models;

import androidx.databinding.BaseObservable;

import com.muva.bamburi.Bamburi;
import com.muva.bamburi.utils.TimeAgo;
import com.muva.bamburi.utils.UniversalMethods;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by Njoro on 5/3/18.
 */
@Entity
public class Newsletter extends BaseObservable{
    @Id(assignable = true)
    private long id;

    private String title;
    private String body;
    private int category;
    private String admin_name;
    private int status;
    private int order_index;
    private String pdf_url;
    private String created_at;
    private String deleted_at;
    private boolean downloaded;

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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public String getAdmin_name() {
        return admin_name;
    }

    public void setAdmin_name(String admin_name) {
        this.admin_name = admin_name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getPdf_url() {
        return pdf_url;
    }

    public void setPdf_url(String pdf_url) {
        this.pdf_url = pdf_url;
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

    public boolean isDownloaded() {
        return UniversalMethods.fileInInternalStorage(Bamburi.getInstance(),getPdf_url());
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public int getOrder_index() {
        return order_index;
    }

    public void setOrder_index(int order_index) {
        this.order_index = order_index;
    }

    public String getTimeCreated() {
        try {
            Date mDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(this.created_at);
            return TimeAgo.toRelative(new Date(mDate.getTime()), new Date(new Date().getTime()), 1);
        } catch (Exception e) {
            return "0secs";
        }
    }
}

package com.muva.bamburi.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by Njoro on 5/3/18.
 */
@Entity
public class NewsLetterCategory {
    @Id(assignable =  true)
    private long id;

    private String name;
    private String desceiption;
    private String admin_name;
    private String created_at;

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

    public String getDesceiption() {
        return desceiption;
    }

    public void setDesceiption(String desceiption) {
        this.desceiption = desceiption;
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
}

package com.muva.bamburi.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by Njoro on 4/24/18.
 */
@Entity
public class User {
    @Id(assignable = true)
    private long id;

    private String name;
    private String email;
    private String staff_id;
    private String phone_number;
    private String id_number;
    private String fcm_token;
    private boolean first_time_login;
    private boolean require_password_reset;
    private int status;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStaff_id() {
        return staff_id;
    }

    public void setStaff_id(String staff_id) {
        this.staff_id = staff_id;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getId_number() {
        return id_number;
    }

    public void setId_number(String id_number) {
        this.id_number = id_number;
    }

    public String getFcm_token() {
        return fcm_token;
    }

    public void setFcm_token(String fcm_token) {
        this.fcm_token = fcm_token;
    }

    public boolean isFirst_time_login() {
        return first_time_login;
    }

    public void setFirst_time_login(boolean first_time_login) {
        this.first_time_login = first_time_login;
    }

    public boolean isRequire_password_reset() {
        return require_password_reset;
    }

    public void setRequire_password_reset(boolean require_password_reset) {
        this.require_password_reset = require_password_reset;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "id: "+id+"\n" +
                "name: "+name+"\n" +
                "email: "+email+"\n" +
                "fcm token: "+fcm_token+"\n";
    }
}

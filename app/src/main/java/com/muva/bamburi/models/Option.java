package com.muva.bamburi.models;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

/**
 * Created by Njoro on 4/26/18.
 */
@Entity
public class Option {
    @Id(assignable = true)
    private long id;

    private int poll_id;
    private String option;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPoll_id() {
        return poll_id;
    }

    public void setPoll_id(int poll_id) {
        this.poll_id = poll_id;
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    @Override
    public String toString() {
        return "\nid: " + id + "\t" + "poll id: " + poll_id + "\t" + "option: " + option + "\t";
    }
}

package com.muva.bamburi.models;

import java.util.List;

/**
 * Created by Njoro on 4/25/18.
 */
public class Data {
    private int viewType;
    private long itemId;
    private List<Poll> pollList;

    public List<Poll> getPollList() {
        return pollList;
    }

    public void setPollList(List<Poll> pollList) {
        this.pollList = pollList;
    }

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }
}

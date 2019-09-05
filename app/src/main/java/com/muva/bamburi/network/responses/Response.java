package com.muva.bamburi.network.responses;

import java.util.List;

/**
 * Created by Njoro on 4/24/18.
 */
public class Response<T> {
    private boolean success;
    private int code;
    private String message;
    private T datum;
    private List<T> data;

    public Response(boolean success, int code, String message, T datum, List<T> data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.datum = datum;
        this.data = data;
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getDatum() {
        return datum;
    }

    public void setDatum(T datum) {
        this.datum = datum;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "success: "+success+"\n" +
                "message: "+message+"\n" +
                "code: "+code+"\n" +
                "datum: "+datum+"\n" +
                "data: "+data+"\n";
    }
}

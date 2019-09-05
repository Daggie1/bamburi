package com.muva.bamburi.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Njoro on 4/24/18.
 */
public class Settings {

    private SharedPreferences settings;
    private Context context;

    private static final String PREFS_NAME = "my_preferences";
    private static final String USER_LOGGED_IN = "user_loggedin";
    private static final String USER_LOGGEDIN_ID = "user_loggedin_id";
    private static final String USER_FIRST_TIME_LOGIN = "user_first_time_login";
    private static final String REQUIRE_PASSWORD_RESET = "require_password_reset";
    private static final String FCM_TOKEN = "FCM_TOKEN";
    private static final String FCM_TOKEN_UPDATED = "FCM_TOKEN_UPDATED";
    private static final String USER_TIMEOUT = "USER_TIMEOUT";
    private static final String NEWS_FETCHED = "NEWS_FETCHED";
    private static final String NEWSLETTERS_FETCHED = "NEWSLETTERS_FETCHED";
    private static final String VIDEOS_FETCHED = "VIDEOS_FETCHED";
    private static final String PHOTOS_FETCHED = "PHOTOS_FETCHED";
    private static final String POLLS_FETCHED = "POLLS_FETCHED";
    private static final String USER_EXISTS = "USER_EXISTS";
    private static final String PHOTO_CORNER_ID = "PHOTO_CORNER_ID";
    private static final String NEWS_ID = "NEWS_ID";
    private static final String PHOTO_CORNER_COL = "PHOTO_CORNER_COL";


    public Settings(Context context) {
        if (context != null) {
            settings = context.getSharedPreferences(PREFS_NAME, 0);
            this.context = context;
        } else {
            L.d("Settings constructor: context is null");
        }
    }

    public void setUserLoggedIn(boolean loggedIn) {
        settings.edit().putBoolean(USER_LOGGED_IN, loggedIn).apply();
    }

    public boolean isUserLoggedIn() {
        return settings.getBoolean(USER_LOGGED_IN, false);
    }

    public void setUserLoggedinId(long user_id) {
        settings.edit().putLong(USER_LOGGEDIN_ID, user_id).apply();
    }

    public long getUserLoggedinId() {
        return settings.getLong(USER_LOGGEDIN_ID, 0);
    }

    public void setUserFirstTimeLogin(boolean firstTimeLogin) {
        settings.edit().putBoolean(USER_FIRST_TIME_LOGIN, firstTimeLogin).apply();
    }

    public boolean isUserFirstTimeLogin() {
        return settings.getBoolean(USER_FIRST_TIME_LOGIN, false);
    }

    public void setRequirePasswordReset(boolean resetPassword){
        settings.edit().putBoolean(REQUIRE_PASSWORD_RESET, resetPassword).apply();
    }

    public boolean isRequirePasswordReset() {
        return settings.getBoolean(REQUIRE_PASSWORD_RESET, false);
    }

    public void setFcmToken(String fcmToken) {
        settings.edit().putString(FCM_TOKEN, fcmToken).apply();
    }

    public String getFcmToken() {
        return settings.getString(FCM_TOKEN, "");
    }

    public void logout() {
        settings.edit().clear().apply();
    }

    public void setUserTimeOut(boolean status) {
        settings.edit().putBoolean(USER_TIMEOUT, status).apply();
    }

    public boolean isUserTimedOut() {
        return settings.getBoolean(USER_TIMEOUT, false);
    }

    public void setUserExists(boolean status) {
        settings.edit().putBoolean(USER_EXISTS,status).apply();
    }

    public boolean isUserExists() {
        return settings.getBoolean(USER_EXISTS, true);
    }

    public void setFcmTokenUpdated(boolean status) {
        settings.edit().putBoolean(FCM_TOKEN_UPDATED, status).apply();
    }

    public boolean isFcmTokenUpdated() {
        return settings.getBoolean(FCM_TOKEN_UPDATED, true);
    }

    public boolean getUserTimeOut() {
        return settings.getBoolean(USER_TIMEOUT, false);
    }

    public void setNewsFetched(boolean state) {
        settings.edit().putBoolean(NEWS_FETCHED, state).apply();
    }

    public boolean isNewsFetched() {
        return settings.getBoolean(NEWS_FETCHED, false);
    }

    public void setNewslettersFetched(boolean state) {
        settings.edit().putBoolean(NEWSLETTERS_FETCHED, state).apply();
    }

    public boolean isNewslettersFetched() {
        return settings.getBoolean(NEWSLETTERS_FETCHED, false);
    }

    public void setVideosFetched(boolean state) {
        settings.edit().putBoolean(NEWS_FETCHED, state).apply();
    }

    public boolean isVideosFetched() {
        return settings.getBoolean(VIDEOS_FETCHED, false);
    }

    public void setPhotosFetched(boolean state) {
        settings.edit().putBoolean(PHOTOS_FETCHED, state).apply();
    }

    public boolean isPhotosFetched() {
        return settings.getBoolean(PHOTOS_FETCHED, false);
    }

    public void setPollsFetched(boolean state) {
        settings.edit().putBoolean(POLLS_FETCHED, state).apply();
    }

    public boolean isPollsFetched() {
        return settings.getBoolean(POLLS_FETCHED, false);
    }

    public void resetDataFetch() {
        setNewsFetched(false);
        setPollsFetched(false);
        setNewslettersFetched(false);
        setVideosFetched(false);
        setPhotosFetched(false);
    }

    public int getPhotoCornerCol() {
        return settings.getInt(PHOTO_CORNER_COL,0);
    }

    public void setPhotoCornerCol(int column_count) {
        settings.edit().putInt(PHOTO_CORNER_COL, 2).apply();
    }

    public long getPhotoCornerId() {
        return settings.getLong(PHOTO_CORNER_ID,0);
    }

    public void setPhotoCornerId(long id) {
        settings.edit().putLong(PHOTO_CORNER_ID, id).apply();
    }

    public Long getNewsId() {
        return settings.getLong(NEWS_ID, 0);
    }

    public void setNewsId(long newsId) {
        settings.edit().putLong(NEWS_ID, newsId).apply();
    }
}

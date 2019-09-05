package com.muva.bamburi.network;

/**
 * Created by Njoro on 4/26/18.
 */
public interface Urls {
//    String LIVE = "https://m-bamburi.com/";  //ENTER LIVE URL
    //String LIVE = "http://192.168.0.105:8000/";  //ENTER LIVE URL
    String LOCAL = "http://192.168.43.12:8000/";  //local url
    String ENV_URL = LOCAL; //either LOCAL OR LIVE
    //    String ENV_URL = LOCAL; //either LOCAL OR LIVE
    String API_URL = ENV_URL + "api/"; //ENTER API URL
    String BASE_MEDIA_URL = ENV_URL + "uploads/"; //ENTER URL TO ACCESS RESOURCES e.g. files, images, videos
    String PHOTOS_URL = BASE_MEDIA_URL + "photos/";
    String NEWSLETTERS_URL = BASE_MEDIA_URL + "newsletters/";
    String NEWS_URL = BASE_MEDIA_URL + "news/";
    String VIDEOS_URL = BASE_MEDIA_URL + "videos/";
    String VIDEO_THUMBNAILS_URL = VIDEOS_URL + "thumbnails/";
}

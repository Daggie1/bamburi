package com.muva.bamburi.network;

import com.muva.bamburi.models.AppVersion;
import com.muva.bamburi.models.News;
import com.muva.bamburi.models.NewsLetterCategory;
import com.muva.bamburi.models.Newsletter;
import com.muva.bamburi.models.Photo;
import com.muva.bamburi.models.PhotoCorner;
import com.muva.bamburi.models.Poll;
import com.muva.bamburi.models.User;
import com.muva.bamburi.models.Video;
import com.muva.bamburi.network.responses.Response;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Njoro on 4/24/18.
 */
public interface ApiEndpoints {

    //auth endpoints
    //start
    @FormUrlEncoded
    @POST("auth/login")
    Single<Response<User>> emailLogin(
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("v2/auth/login")
    Single<Response<User>> phoneLogin(
            @Field("phone_no") String phoneNo,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("auth/forgotPassword")
    Single<Response<User>> forgotPassword(
            @Field("email") String email
    );

    @FormUrlEncoded
    @POST("v2/auth/forgotPassword")
    Single<Response<User>> phoneForgotPassword(
            @Field("phone_no") String phoneNo
    );

    @FormUrlEncoded
    @POST("auth/resetPassword")
    Single<Response<User>> resetPassword(
            @Field("email") String email,
            @Field("old_password") String currentPassword,
            @Field("new_password") String newPassword
    );

    @FormUrlEncoded
    @POST("v2/auth/resetPassword")
    Single<Response<User>> resetPasswordPhone(
            @Field("phone_no") String email,
            @Field("old_password") String currentPassword,
            @Field("new_password") String newPassword
    );

    @FormUrlEncoded
    @POST("auth/updateFCMToken")
    Single<Response<User>> updateFCMToken(
            @Field("user_id") String user_id,
            @Field("fcm_token") String fcm_token
    );

    //end

    //fetch polls
    @GET("polls/users/{user_id}")
    Single<Response<Poll>> getPolls(
            @Path("user_id") long user_id
    );

    //reply to poll
    @FormUrlEncoded
    @POST("polls/{poll_id}/replies")
    Single<Response<Poll>> pollReply(
            @Path("poll_id") long poll_id,
            @Field("user_id") long user_id,
            @Field("poll_option_id") long poll_option_id
    );

    //fetch news
    @GET("news/users/{user_id}")
    Single<Response<News>> getNews(
            @Path("user_id") long user_id
    );

    //reply to news
    @FormUrlEncoded
    @POST("news/{news_id}/responses")
    Single<Response<News>> postNewsResponse(
            @Path("news_id") long news_id,
            @Field("user_id") long user_id,
            @Field("response") String response
    );

    //fetch newsletter categories
    @GET("newsletters_categories")
    Single<Response<NewsLetterCategory>> getNewsletterCategories(
            @Query("user_id") long user_id
    );

    //fetch newsletters
    @GET("newsletters")
    Single<Response<Newsletter>> getNewsletters(
            @Query("user_id") long user_id
    );

    //fetch photos corners
    @GET("photos_corners")
    Single<Response<PhotoCorner>> getPhotoCorners(
            @Query("user_id") long user_id
    );

    //fetch photos for specific photo corner
    @GET("photos_corners/{id}")
    Single<Response<Photo>> getPhotos(
            @Query("user_id") long user_id,
            @Path("id") int id
    );

    //fetch videos
    @GET("videos")
    Single<Response<Video>> getVideos(
            @Query("user_id") long user_id
    );

    //send feedback
    @FormUrlEncoded
    @POST("send_feedback")
    Single<Response<User>> sendFeedback(
            @Field("user_id") long user_id,
            @Field("title") String title,
            @Field("comment") String comment
    );
    @GET("{version_code}")
    Call<Response<AppVersion>> getVersionCode(@Path("version_code") double versionCode);

}

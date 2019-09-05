package com.muva.bamburi.network;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.muva.bamburi.utils.L;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Njoro on 4/26/18.
 */
public class RetrofitService {

        public static ApiEndpoints getInstance(){
        OkHttpClient client =
                   new OkHttpClient
                           .Builder()
                           .connectTimeout(1, TimeUnit.MINUTES)
                           .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                           .addInterceptor(chain -> {
                               Request original = chain.request();
                               Request.Builder builder = original.newBuilder()
                               //      .addHeader("Authorization", "Bearer "+token)
                                       .addHeader("Accept", "application/vnd.bamburiapp.v1+json");                                Request request = builder.build();
                                       return chain.proceed(request);
                           })
                           .addInterceptor(new UnauthorizedResponseInterceptor())
                           .build();

                           L.d("API URL: " + Urls.API_URL);


           Retrofit retrofit = new Retrofit.Builder()
                   .client(client)
                   .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                   .addConverterFactory(GsonConverterFactory.create())
                   .baseUrl(Urls.API_URL)
                   .build();
                   return retrofit.create(ApiEndpoints.class);
    }
}

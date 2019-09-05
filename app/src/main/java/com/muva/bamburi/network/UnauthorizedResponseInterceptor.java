package com.muva.bamburi.network;

import com.google.gson.Gson;
import com.muva.bamburi.Bamburi;
import com.muva.bamburi.utils.L;
import com.muva.bamburi.utils.Settings;
import com.muva.bamburi.utils.UniversalMethods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by Njoro on 5/29/18.
 */
public class UnauthorizedResponseInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        Settings settings = new Settings(Bamburi.getInstance());
        if (response.code() == 401) {
            L.e(response.message());

            try {

                Gson gson = new Gson();
                if (response.body() != null) {
//                    L.e("response body: "+ response.body().string());

                    InputStream i = response.body().byteStream();
                    BufferedReader r = new BufferedReader(new InputStreamReader(i));
                    StringBuilder errorResult = new StringBuilder();
                    String line;
                    try {
                        while ((line = r.readLine()) != null) {
                            errorResult.append(line).append('\n');
                        }

                        com.muva.bamburi.network.responses.Response response1 = gson.fromJson(errorResult.toString(), com.muva.bamburi.network.responses.Response.class);
                        if (response1 != null) {
                            L.e("the response: "+response1.toString());
//                            if (response1.getCode() == 1) {
//                                //password reset required
//                                settings.setRequirePasswordReset(true);
//                            UniversalMethods.checkUserStatus(Bamburi.getInstance());
//                            } else
                                if (response1.getCode() == 2) {
                                //inactive
                                settings.setUserTimeOut(true);
                                UniversalMethods.checkUserStatus(Bamburi.getInstance());
//                                L.T(Bamburi.getInstance(),"Your account is suspended. Contact the system admin");
                            } else if (response1.getCode() == 3) {
                                settings.setUserExists(false);
                                UniversalMethods.checkUserStatus(Bamburi.getInstance());
                            }
                        }else{
                            L.e("response is null");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }finally {
                        response.close();
                    }

                }
            } catch (Exception exception) {
                L.e("error when reading response: "+exception.getMessage()+"\n "+exception.getLocalizedMessage());
                exception.printStackTrace();
            }

        } else if (response.code() == 403) {
            //password reset required
            settings.setRequirePasswordReset(true);
            UniversalMethods.checkUserStatus(Bamburi.getInstance());
        }
        return response;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.siri.service;

import com.siri.main.EntryPoint;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Security;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 *
 * @author ronald.langat
 */
public class Utilities {

    static String TOKEN_URL = EntryPoint.TOKEN_URL;

    public static String getAccessToken(String app_key, String app_secret) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        String access_token = "";
        try {

            String appKeySecret = app_key + ":" + app_secret;
            byte[] bytes = appKeySecret.getBytes("ISO-8859-1");
            String auth = Base64.encode(bytes);

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(TOKEN_URL)
                    .get()
                    .addHeader("authorization", "Basic " + auth)
                    .addHeader("cache-control", "no-cache")
                    .build();

           Response response = client.newCall(request).execute();
            // System.out.println("response.body().string() "+response.body().string());
            JsonObject accessT = new JsonObject(response.body().string());

            access_token = accessT.getString("access_token");

            response.close();
        } catch (UnsupportedEncodingException ex) {
            access_token = "";
            ex.printStackTrace();
        } catch (IOException ex) {
            access_token = "";
            ex.printStackTrace();
        }
        // System.out.println("access_token r  : " + access_token);

        return access_token;
    }
}

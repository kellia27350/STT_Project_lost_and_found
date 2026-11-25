package com.auca.lostandfound.network;

import com.auca.lostandfound.BuildConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton Retrofit client.
 *
 * BASE_URL is injected from BuildConfig so it can differ between
 * debug builds (emulator localhost) and release builds (production host).
 */
public class ApiClient {

    private static ApiService instance;

    private ApiClient() {}

    public static ApiService get() {
        if (instance == null) {
            instance = buildRetrofit().create(ApiService.class);
        }
        return instance;
    }

    private static Retrofit buildRetrofit() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        // Log request/response bodies in debug builds
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);
        }

        // Lenient Gson — tolerates extra fields the backend may add later
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(httpClient.build())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }
}

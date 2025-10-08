package com.example.prm392_finalproject_productsaleapp_group2.net;

import com.example.prm392_finalproject_productsaleapp_group2.services.ProfileApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static ProfileApiClient instance;
    private ProfileApiService apiService;

    private ProfileApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        apiService = retrofit.create(ProfileApiService.class);
    }

    public static synchronized ProfileApiClient getInstance() {
        if (instance == null) {
            instance = new ProfileApiClient();
        }
        return instance;
    }

    public ProfileApiService getApiService() {
        return apiService;
    }
}

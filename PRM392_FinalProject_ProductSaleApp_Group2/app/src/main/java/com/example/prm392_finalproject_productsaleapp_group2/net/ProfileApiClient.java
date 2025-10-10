package com.example.prm392_finalproject_productsaleapp_group2.net;

import com.example.prm392_finalproject_productsaleapp_group2.services.ProfileApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProfileApiClient {
    private static ProfileApiClient instance;
    private ProfileApiService apiService;

    private ProfileApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
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

package com.example.prm392_finalproject_productsaleapp_group2.net;

import com.example.prm392_finalproject_productsaleapp_group2.services.AuthApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AuthApiClient {
    private static AuthApiClient instance;
    private AuthApiService apiService;

    private AuthApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        apiService = retrofit.create(AuthApiService.class);
    }

    public static synchronized AuthApiClient getInstance() {
        if (instance == null) {
            instance = new AuthApiClient();
        }
        return instance;
    }

    public AuthApiService getApiService() {
        return apiService;
    }
}


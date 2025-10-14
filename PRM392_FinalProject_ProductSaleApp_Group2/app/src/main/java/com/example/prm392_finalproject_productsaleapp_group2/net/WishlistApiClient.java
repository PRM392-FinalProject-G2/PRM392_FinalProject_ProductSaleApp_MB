package com.example.prm392_finalproject_productsaleapp_group2.net;

import com.example.prm392_finalproject_productsaleapp_group2.services.WishlistApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WishlistApiClient {
    private static WishlistApiClient instance;
    private WishlistApiService apiService;

    private WishlistApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        apiService = retrofit.create(WishlistApiService.class);
    }

    public static synchronized WishlistApiClient getInstance() {
        if (instance == null) {
            instance = new WishlistApiClient();
        }
        return instance;
    }

    public WishlistApiService getApiService() {
        return apiService;
    }
}
package com.example.prm392_finalproject_productsaleapp_group2.net;

import com.example.prm392_finalproject_productsaleapp_group2.services.BrandApiService;
import com.example.prm392_finalproject_productsaleapp_group2.services.CategoryApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BrandApiClient {
    private static BrandApiClient instance;
    private BrandApiService apiService;

    private BrandApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(BrandApiService.class);
    }

    public static synchronized BrandApiClient getInstance() {
        if (instance == null) {
            instance = new BrandApiClient();
        }
        return instance;
    }

    public BrandApiService getApiService() {
        return apiService;
    }
}

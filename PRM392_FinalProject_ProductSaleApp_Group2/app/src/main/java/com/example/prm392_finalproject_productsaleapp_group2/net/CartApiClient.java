package com.example.prm392_finalproject_productsaleapp_group2.net;

import com.example.prm392_finalproject_productsaleapp_group2.services.CartApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CartApiClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static CartApiClient instance;
    private CartApiService apiService;

    private CartApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        
        apiService = retrofit.create(CartApiService.class);
    }

    public static synchronized CartApiClient getInstance() {
        if (instance == null) {
            instance = new CartApiClient();
        }
        return instance;
    }

    public CartApiService getApiService() {
        return apiService;
    }
}


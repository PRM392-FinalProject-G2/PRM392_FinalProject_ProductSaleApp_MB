package com.example.prm392_finalproject_productsaleapp_group2.net;

import com.example.prm392_finalproject_productsaleapp_group2.services.OrderApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OrderApiClient {
    private static OrderApiClient instance;
    private OrderApiService apiService;

    private OrderApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(OrderApiService.class);
    }

    public static synchronized OrderApiClient getInstance() {
        if (instance == null) {
            instance = new OrderApiClient();
        }
        return instance;
    }

    public OrderApiService getApiService() {
        return apiService;
    }
}

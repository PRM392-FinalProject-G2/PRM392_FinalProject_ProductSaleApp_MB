package com.example.prm392_finalproject_productsaleapp_group2.net;

import com.example.prm392_finalproject_productsaleapp_group2.services.AuthApiService;
import com.example.prm392_finalproject_productsaleapp_group2.services.ProductApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductApiClient {
    private static ProductApiClient instance;
    private ProductApiService apiService;

    private ProductApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ProductApiService.class);
    }

    public static synchronized ProductApiClient getInstance() {
        if (instance == null) {
            instance = new ProductApiClient();
        }
        return instance;
    }

    public ProductApiService getApiService() {
        return apiService;
    }
}

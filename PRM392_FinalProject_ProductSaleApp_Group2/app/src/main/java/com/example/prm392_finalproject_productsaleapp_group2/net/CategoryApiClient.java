package com.example.prm392_finalproject_productsaleapp_group2.net;

import com.example.prm392_finalproject_productsaleapp_group2.services.CategoryApiService;
import com.example.prm392_finalproject_productsaleapp_group2.services.ProductApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CategoryApiClient {
    private static CategoryApiClient instance;
    private CategoryApiService apiService;

    private CategoryApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(CategoryApiService.class);
    }

    public static synchronized CategoryApiClient getInstance() {
        if (instance == null) {
            instance = new CategoryApiClient();
        }
        return instance;
    }

    public CategoryApiService getApiService() {
        return apiService;
    }
}

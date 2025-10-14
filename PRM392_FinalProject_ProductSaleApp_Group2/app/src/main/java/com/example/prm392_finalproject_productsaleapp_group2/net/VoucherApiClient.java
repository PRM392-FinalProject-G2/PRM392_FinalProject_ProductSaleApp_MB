package com.example.prm392_finalproject_productsaleapp_group2.net;

import com.example.prm392_finalproject_productsaleapp_group2.services.VoucherApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class VoucherApiClient {
    private static VoucherApiClient instance;
    private VoucherApiService apiService;

    private VoucherApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(VoucherApiService.class);
    }

    public static synchronized VoucherApiClient getInstance() {
        if (instance == null) {
            instance = new VoucherApiClient();
        }
        return instance;
    }

    public VoucherApiService getApiService() {
        return apiService;
    }
}






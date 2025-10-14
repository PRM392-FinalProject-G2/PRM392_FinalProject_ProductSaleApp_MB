package com.example.prm392_finalproject_productsaleapp_group2.net;

import com.example.prm392_finalproject_productsaleapp_group2.services.PaymentApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaymentApiClient {
    private static PaymentApiClient instance;
    private PaymentApiService apiService;

    private PaymentApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(PaymentApiService.class);
    }

    public static synchronized PaymentApiClient getInstance() {
        if (instance == null) {
            instance = new PaymentApiClient();
        }
        return instance;
    }

    public PaymentApiService getApiService() {
        return apiService;
    }
}







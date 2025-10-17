package com.example.prm392_finalproject_productsaleapp_group2.net;

import com.example.prm392_finalproject_productsaleapp_group2.services.ChatApiService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ChatApiClient {
    private static ChatApiClient instance;
    private final ChatApiService apiService;

    private ChatApiClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiConfig.BASE_URL + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ChatApiService.class);
    }

    public static synchronized ChatApiClient getInstance() {
        if (instance == null) {
            instance = new ChatApiClient();
        }
        return instance;
    }

    public ChatApiService getApiService() {
        return apiService;
    }
}



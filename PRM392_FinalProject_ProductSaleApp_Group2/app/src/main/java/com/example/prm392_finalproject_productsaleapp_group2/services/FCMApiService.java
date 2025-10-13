package com.example.prm392_finalproject_productsaleapp_group2.services;

import com.example.prm392_finalproject_productsaleapp_group2.models.FCMTokenRequest;
import com.example.prm392_finalproject_productsaleapp_group2.models.FCMTokenResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FCMApiService {
    
    @POST("api/UserDeviceTokens/register")
    Call<FCMTokenResponse> registerToken(@Body FCMTokenRequest request);

    @POST("api/UserDeviceTokens/deactivate")
    Call<Void> deactivateToken(@Body FCMTokenRequest request);
}

